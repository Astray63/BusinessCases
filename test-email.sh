#!/bin/bash

# Script de test complet pour l'envoi d'email

echo "🧪 Test complet d'envoi d'email"
echo "=========================================="

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

EMAIL="astray63000@gmail.com"

# Étape 1 : Inscription d'un nouvel utilisateur
echo ""
echo -e "${YELLOW}� Étape 1 : Inscription d'un nouvel utilisateur...${NC}"
echo ""

REGISTER_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "utilisateur": {
      "nom": "Test",
      "prenom": "User",
      "pseudo": "testuser_'$(date +%s)'",
      "email": "'$EMAIL'",
      "dateNaissance": "1990-01-01",
      "role": "client",
      "iban": "",
      "adressePhysique": "",
      "medias": ""
    },
    "motDePasse": "password123"
  }')

REGISTER_CODE=$(echo "$REGISTER_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
REGISTER_BODY=$(echo "$REGISTER_RESPONSE" | grep -v "HTTP_CODE")

echo "Status HTTP: $REGISTER_CODE"

if [ "$REGISTER_CODE" = "201" ]; then
    echo -e "${GREEN}✅ Inscription réussie !${NC}"
    echo ""
    echo -e "${GREEN}� Un email de vérification devrait être envoyé à: $EMAIL${NC}"
    echo ""
    echo "�📬 Vérifiez maintenant:"
    echo "   1. Votre boîte de réception"
    echo "   2. Le dossier spam/courrier indésirable"
    echo "   3. Les logs du backend pour voir les détails d'envoi"
    echo ""
    echo -e "${YELLOW}⏰ L'email peut prendre 1-2 minutes à arriver${NC}"
    
    # Proposer de renvoyer le code
    echo ""
    echo "=========================================="
    echo ""
    read -p "Voulez-vous renvoyer le code de vérification ? (o/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        echo ""
        echo -e "${YELLOW}📧 Renvoi du code de vérification...${NC}"
        echo ""
        
        RESEND_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST http://localhost:8080/api/auth/resend-verification \
          -H "Content-Type: application/json" \
          -d '{"email": "'$EMAIL'"}')
        
        RESEND_CODE=$(echo "$RESEND_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
        RESEND_BODY=$(echo "$RESEND_RESPONSE" | grep -v "HTTP_CODE")
        
        echo "Status HTTP: $RESEND_CODE"
        
        if [ "$RESEND_CODE" = "200" ]; then
            echo -e "${GREEN}✅ Code renvoyé avec succès !${NC}"
            echo "� Vérifiez à nouveau votre boîte email"
        else
            echo -e "${RED}❌ Erreur lors du renvoi${NC}"
            echo "Réponse: $RESEND_BODY"
        fi
    fi
else
    echo -e "${RED}❌ Erreur lors de l'inscription${NC}"
    echo "Réponse: $REGISTER_BODY"
    echo ""
    echo "💡 Cela peut arriver si un utilisateur avec cet email existe déjà."
    echo "   Dans ce cas, testez directement le renvoi du code:"
    echo ""
    echo "   curl -X POST http://localhost:8080/api/auth/resend-verification \\"
    echo "     -H \"Content-Type: application/json\" \\"
    echo "     -d '{\"email\": \"$EMAIL\"}'"
fi

echo ""
echo "=========================================="
