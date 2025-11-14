# 📧 Configuration Email - Guide Rapide

## ✅ Ce qui a été fait

### Backend (Spring Boot)

1. **Service d'envoi d'emails** (`EmailService`)
   - Utilisation de l'API Brevo (SendInBlue)
   - Templates HTML pour emails de vérification et bienvenue
   - Gestion des erreurs

2. **Gestion de la vérification**
   - Génération de code à 6 chiffres
   - Expiration après 15 minutes
   - Stockage dans la base de données

3. **Nouveaux endpoints** (`AuthController`)
   - `POST /api/auth/verify-email` - Valider le code
   - `POST /api/auth/resend-verification` - Renvoyer le code

4. **Modèle mis à jour** (`Utilisateur`)
   - Ajout du champ `verificationCodeExpiry`
   - Gestion de `emailVerified`

### Frontend (Angular)

1. **Service de vérification** (`EmailVerificationService`)
   - Communication avec l'API backend
   - Gestion des requêtes

2. **Page de vérification** (`VerifyEmailComponent`)
   - Interface utilisateur moderne
   - Validation du code
   - Renvoi du code
   - Responsive design

3. **Routing mis à jour**
   - Route `/auth/verify-email`
   - Redirection après inscription

## 🚀 Pour démarrer

### 1. Configurer Brevo

```bash
# 1. Créez un compte sur https://www.brevo.com
# 2. Obtenez votre clé API
# 3. Vérifiez votre email expéditeur
```

### 2. Configuration Backend

Modifiez `backend/src/main/resources/application.properties` :

```properties
brevo.api.key=VOTRE_CLE_API_ICI
brevo.sender.email=votre-email@exemple.com
brevo.sender.name=Electric Charge Platform
email.verification.code.expiry-minutes=15
```

### 3. Mettre à jour la base de données

**Option A - Recréer la base** (développement)
```bash
# Spring va recréer automatiquement avec ddl-auto=create-drop
```

**Option B - Migration manuelle**
```sql
ALTER TABLE utilisateur 
ADD COLUMN verification_code_expiry TIMESTAMP;
```

### 4. Tester

1. Démarrez le backend :
```bash
cd backend
./mvnw spring-boot:run
```

2. Démarrez le frontend :
```bash
cd frontend
npm start
```

3. Inscrivez-vous avec une vraie adresse email
4. Vérifiez votre boîte de réception
5. Entrez le code à 6 chiffres

## 📋 Flux utilisateur

```
1. Utilisateur s'inscrit
   ↓
2. Backend génère code à 6 chiffres
   ↓
3. Email envoyé via Brevo
   ↓
4. Utilisateur redirigé vers /auth/verify-email
   ↓
5. Utilisateur entre le code
   ↓
6. Backend vérifie le code et l'expiration
   ↓
7. Email de bienvenue envoyé
   ↓
8. Utilisateur redirigé vers /auth/login
```

## 🔍 Résolution de problèmes

### L'email n'arrive pas
- ✅ Vérifiez la clé API Brevo
- ✅ Vérifiez que l'email expéditeur est validé dans Brevo
- ✅ Consultez les logs backend
- ✅ Regardez dans les spams

### Code invalide
- ✅ Le code expire après 15 minutes
- ✅ Utilisez "Renvoyer le code"
- ✅ Un nouveau code remplace l'ancien

### Erreur de compilation
```bash
# Backend
cd backend
./mvnw clean install

# Frontend
cd frontend
npm install
```

## 📊 Limites Brevo (plan gratuit)

- **300 emails/jour**
- Suffisant pour le développement
- Pour production : envisager un plan payant

## 📁 Fichiers créés/modifiés

### Backend
```
✅ EmailService.java
✅ EmailServiceImpl.java
✅ VerifyEmailRequestDto.java
✅ ResendVerificationRequestDto.java
✅ UtilisateurServiceImpl.java (modifié)
✅ AuthController.java (modifié)
✅ Utilisateur.java (modifié)
✅ application.properties (modifié)
```

### Frontend
```
✅ email-verification.service.ts
✅ verify-email.component.ts
✅ verify-email.component.html
✅ verify-email.component.scss
✅ auth.module.ts (modifié)
✅ register.component.ts (modifié)
```

### Documentation
```
✅ EMAIL_SETUP.md (guide complet)
✅ brevo-config.example (exemple de configuration)
✅ CONFIGURATION_EMAIL.md (ce fichier)
```

## 🎯 Prochaines étapes suggérées

1. ✨ Ajouter la récupération de mot de passe par email
2. 📧 Notifications par email pour les réservations
3. 🎨 Personnaliser les templates d'emails
4. 📊 Dashboard pour suivre les emails envoyés
5. 🔔 Emails de rappel pour les réservations

## 💡 Remarques importantes

- Le système d'email ne bloque pas l'inscription si l'envoi échoue
- Les erreurs d'email sont loggées mais n'affectent pas l'UX
- Un utilisateur ne peut pas se connecter avant vérification
- Les codes expirés doivent être renouvelés

## 📞 Support

Pour plus d'informations, consultez `EMAIL_SETUP.md`
