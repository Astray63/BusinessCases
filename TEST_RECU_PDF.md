# Guide de test - Reçu PDF pour réservations

## 🧪 Comment tester la fonctionnalité

### Prérequis
- Backend démarré sur `http://localhost:8080`
- Frontend démarré sur `http://localhost:4200`
- Utilisateur connecté (client ET propriétaire de borne)

### Scénario de test complet

#### 1️⃣ Créer une réservation (en tant que CLIENT)

**Endpoint :** `POST /api/reservations`

**Body :**
```json
{
  "utilisateurId": 1,
  "chargingStationId": 2,
  "dateDebut": "2025-11-20T10:00:00",
  "dateFin": "2025-11-20T12:00:00"
}
```

**Réponse attendue :**
```json
{
  "result": "SUCCESS",
  "message": "Réservation créée",
  "data": {
    "id": 15,
    "etat": "EN_ATTENTE",
    "receiptPath": null
  }
}
```

✅ La réservation est créée avec le statut `EN_ATTENTE`

---

#### 2️⃣ Accepter la réservation (en tant que PROPRIÉTAIRE)

**Endpoint :** `PUT /api/reservations/15/accepter`

**Body :**
```json
{
  "proprietaireId": 10
}
```

**Réponse attendue :**
```json
{
  "result": "SUCCESS",
  "message": "Réservation acceptée avec succès. Un reçu PDF a été généré.",
  "data": {
    "id": 15,
    "etat": "CONFIRMEE",
    "receiptPath": "./storage/receipts/recu_reservation_15_1234567890.pdf"
  }
}
```

✅ La réservation passe à `CONFIRMEE`
✅ Un PDF est généré dans `./storage/receipts/`

---

#### 3️⃣ Télécharger le reçu PDF

**Endpoint :** `GET /api/reservations/15/receipt`

**Headers :**
```
Authorization: Bearer <votre_token_jwt>
```

**Réponse attendue :**
- Status: `200 OK`
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="recu_reservation_15.pdf"`
- Body: Contenu binaire du PDF

✅ Le PDF est téléchargé avec succès

---

### 🌐 Test depuis le Frontend

#### Interface utilisateur - Onglet Client

1. **Se connecter en tant que client**
2. **Aller sur "Réservations"**
3. **Créer une nouvelle réservation** (onglet "Nouvelle réservation")
4. La réservation apparaît dans **"En cours"** avec le statut **"En attente"**

#### Interface utilisateur - Onglet Propriétaire

1. **Se connecter en tant que propriétaire** (ou utiliser un compte qui possède des bornes)
2. **Aller sur "Réservations" > "Mes bornes"**
3. **Voir la demande en attente**
4. **Cliquer sur "Accepter"**
5. ✅ La réservation passe à "Confirmée"
6. ✅ Un bouton **"Télécharger le reçu"** apparaît
7. **Cliquer sur le bouton** pour télécharger le PDF

---

### 📝 Vérifications manuelles

#### Vérifier dans la base de données :

```sql
-- Voir toutes les réservations avec leur statut
SELECT numero_reservation, etat, receipt_path, 
       date_debut, date_fin, total_price
FROM reservation
ORDER BY numero_reservation DESC
LIMIT 10;

-- Voir une réservation spécifique
SELECT * FROM reservation WHERE numero_reservation = 15;
```

#### Vérifier les fichiers PDF :

```bash
# Lister les PDF générés
ls -lh ./storage/receipts/

# Voir le contenu d'un PDF (Linux)
xdg-open ./storage/receipts/recu_reservation_15_*.pdf
```

---

### 🐛 Problèmes courants

#### ❌ "Aucun reçu disponible pour cette réservation"

**Cause :** La réservation n'a pas encore été acceptée par le propriétaire.

**Solution :** 
1. Vérifier que la réservation a le statut `CONFIRMEE`
2. Accepter la réservation via l'endpoint `/accepter`

#### ❌ "Vous n'êtes pas autorisé à accepter cette réservation"

**Cause :** L'utilisateur qui essaie d'accepter n'est pas le propriétaire de la borne.

**Solution :** Utiliser l'ID du vrai propriétaire de la borne dans `proprietaireId`.

#### ❌ "Erreur lors de la génération du reçu PDF"

**Cause :** Problème avec le répertoire de stockage ou permissions.

**Solution :**
```bash
# Créer le répertoire manuellement
mkdir -p ./storage/receipts
chmod 755 ./storage/receipts
```

#### ❌ Le mapping n'est pas reconnu

**Cause :** L'ordre des méthodes dans le controller est incorrect.

**Solution :** Vérifier que `@GetMapping("/{id}/receipt")` est AVANT `@GetMapping("/{id}")`.

---

### 🎯 Résultat attendu du PDF

Le PDF généré contient :

- **En-tête** : Logo "ElectricCharge" + titre
- **Informations client** : Nom, email, pseudo
- **Détails de la borne** : Nom, localisation, puissance, type de connecteur
- **Détails de la réservation** : Dates, durée, prix/minute, statut
- **Montant total** : En gros et en évidence
- **Pied de page** : Remerciements + contact support

---

### 📊 Commandes utiles pour les tests

#### Via cURL :

```bash
# 1. Créer une réservation
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "utilisateurId": 1,
    "chargingStationId": 2,
    "dateDebut": "2025-11-20T10:00:00",
    "dateFin": "2025-11-20T12:00:00"
  }'

# 2. Accepter la réservation
curl -X PUT http://localhost:8080/api/reservations/15/accepter \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"proprietaireId": 10}'

# 3. Télécharger le reçu
curl -X GET http://localhost:8080/api/reservations/15/receipt \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output recu_test.pdf
```

---

### ✅ Checklist de validation

- [ ] Réservation créée avec statut `EN_ATTENTE`
- [ ] Réservation acceptée par le propriétaire
- [ ] Statut passe à `CONFIRMEE`
- [ ] Fichier PDF créé dans `./storage/receipts/`
- [ ] `receipt_path` enregistré en base de données
- [ ] Endpoint `/receipt` retourne le PDF
- [ ] Bouton "Reçu PDF" visible dans le frontend
- [ ] Téléchargement du PDF fonctionne
- [ ] Contenu du PDF correct et complet
- [ ] Message de succès affiché dans l'interface

---

## 🔄 Workflow complet

```
CLIENT                    SYSTÈME                    PROPRIÉTAIRE
  |                          |                            |
  |-- Crée réservation ----->|                            |
  |                          |                            |
  |<---- EN_ATTENTE ---------|                            |
  |                          |                            |
  |                          |<--- Reçoit demande --------|
  |                          |                            |
  |                          |<--- Accepte --------------|
  |                          |                            |
  |                          |--- Génère PDF -------->💾  |
  |                          |                            |
  |                          |--- CONFIRMEE ------------->|
  |                          |                            |
  |<--- Notification --------|                            |
  |                          |                            |
  |--- Télécharge PDF ------>|                            |
  |                          |                            |
  |<--- Reçoit PDF 📄 -------|                            |
```

---

**Date de création :** 17 novembre 2025  
**Status :** ✅ Prêt pour les tests
