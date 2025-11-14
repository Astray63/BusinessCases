# 🔧 Guide de dépannage - Envoi d'emails

## 🔍 Vérifications à faire

### 1. Vérifier les logs
Les logs sont maintenant très détaillés. Cherchez :
```
ERROR ... EmailServiceImpl : Erreur détaillée lors de l'envoi d'email
```

### 2. Vérifier la clé API Brevo

**Dans application.properties :**
```properties
brevo.api.key=xkeysib-VOTRE_CLE_COMPLETE
```

✅ La clé doit commencer par `xkeysib-`
✅ Elle fait environ 70 caractères
✅ Pas d'espaces avant ou après

**Tester la clé API avec curl :**
```bash
curl -X POST "https://api.brevo.com/v3/smtp/email" \
  -H "accept: application/json" \
  -H "api-key: VOTRE_CLE_ICI" \
  -H "content-type: application/json" \
  -d '{
    "sender": {"email": "astray63000@gmail.com", "name": "Test"},
    "to": [{"email": "astray63000@gmail.com", "name": "Test"}],
    "subject": "Test",
    "htmlContent": "<html><body>Test</body></html>"
  }'
```

### 3. Vérifier l'email expéditeur dans Brevo

**Important !** L'email expéditeur doit être vérifié dans Brevo :
1. Connectez-vous sur https://app.brevo.com
2. Allez dans **Settings** → **Senders & IP**
3. Vérifiez que `astray63000@gmail.com` est présent et **vérifié**
4. Si non vérifié, cliquez sur "Verify" et suivez les instructions

### 4. Erreurs communes

#### Erreur : "Unauthorized sender"
```
❌ Sender email not verified
✅ Solution : Vérifiez votre email dans Brevo
```

#### Erreur : "Invalid API key"
```
❌ La clé API est incorrecte ou expirée
✅ Solution : Regénérez une nouvelle clé API
```

#### Erreur : "Daily sending limit reached"
```
❌ Limite de 300 emails/jour atteinte
✅ Solution : Attendez demain ou passez au plan payant
```

#### Erreur : "Connection timeout"
```
❌ Problème de connexion Internet
✅ Solution : Vérifiez votre connexion
```

### 5. Tester l'envoi depuis le backend

**Option A - Via l'API directement :**
```bash
curl -X POST http://localhost:8080/api/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{"email": "astray63000@gmail.com"}'
```

**Option B - Créer un utilisateur de test :**
```bash
# Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "utilisateur": {
      "nom": "Test",
      "prenom": "User",
      "pseudo": "testuser",
      "email": "astray63000@gmail.com",
      "dateNaissance": "1990-01-01",
      "role": "client",
      "iban": "",
      "adressePhysique": "",
      "medias": ""
    },
    "motDePasse": "password123"
  }'
```

### 6. Vérifier les logs détaillés

Avec les nouveaux logs, vous verrez :
```
DEBUG EmailServiceImpl : Tentative d'envoi d'email à: astray63000@gmail.com
DEBUG EmailServiceImpl : API Key présente: true
DEBUG EmailServiceImpl : Sender email: astray63000@gmail.com
DEBUG EmailServiceImpl : Données email préparées pour: astray63000@gmail.com
INFO  EmailServiceImpl : Email envoyé avec succès à: astray63000@gmail.com - Status: 201
```

Ou en cas d'erreur :
```
ERROR EmailServiceImpl : Erreur détaillée lors de l'envoi d'email à astray63000@gmail.com: ...
```

## 🚀 Procédure de test complète

1. **Vérifiez la configuration**
```bash
cat backend/src/main/resources/application.properties | grep brevo
```

2. **Redémarrez le backend**
```bash
cd backend
./mvnw spring-boot:run
```

3. **Testez l'inscription**
- Allez sur http://localhost:4200/auth/register
- Inscrivez-vous avec votre vraie adresse email
- Vérifiez les logs du backend
- Vérifiez votre boîte email (et spams)

4. **Si ça ne marche pas**
- Copiez les logs d'erreur
- Vérifiez la checklist ci-dessus
- Testez avec curl pour isoler le problème

## ✅ Checklist rapide

- [ ] Clé API Brevo configurée
- [ ] Email expéditeur vérifié dans Brevo
- [ ] Backend redémarré
- [ ] Base de données à jour (colonne verification_code_expiry existe)
- [ ] Aucune erreur de compilation
- [ ] Logs activés (DEBUG pour EmailServiceImpl)

## 📞 Besoin d'aide ?

Si le problème persiste, partagez :
1. Les logs d'erreur complets
2. Le résultat de la commande curl de test
3. Screenshot de vos senders dans Brevo

---
**Note :** Avec le plan gratuit Brevo, vous avez droit à 300 emails/jour, largement suffisant pour le développement !
