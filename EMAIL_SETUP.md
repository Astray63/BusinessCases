# Configuration de l'envoi d'emails avec Brevo (SendInBlue)

## 📧 Vue d'ensemble

L'application utilise l'API Brevo (anciennement SendInBlue) pour envoyer des emails de vérification et de bienvenue. Brevo offre un plan gratuit avec **300 emails par jour**.

## 🚀 Configuration

### 1. Créer un compte Brevo

1. Allez sur [https://www.brevo.com](https://www.brevo.com)
2. Créez un compte gratuit
3. Vérifiez votre email

### 2. Obtenir votre clé API

1. Connectez-vous à votre compte Brevo
2. Allez dans **Settings** (Paramètres) → **SMTP & API** → **API Keys**
3. Cliquez sur **Generate a new API key**
4. Donnez un nom à votre clé (ex: "ElectricChargePlatform")
5. Copiez la clé générée

### 3. Configurer l'email expéditeur

1. Dans Brevo, allez dans **Senders & IP** → **Senders**
2. Ajoutez une adresse email d'expéditeur (ex: noreply@votredomaine.com)
3. Vérifiez l'adresse email via le lien envoyé

**Note:** Si vous n'avez pas de domaine, vous pouvez utiliser l'email avec lequel vous vous êtes inscrit sur Brevo.

### 4. Mettre à jour application.properties

Modifiez le fichier `backend/src/main/resources/application.properties` :

```properties
# Brevo Email API Configuration
brevo.api.key=VOTRE_CLE_API_BREVO_ICI
brevo.sender.email=votre-email@exemple.com
brevo.sender.name=Electric Charge Platform

# Email verification settings
email.verification.code.expiry-minutes=15
```

Remplacez :
- `VOTRE_CLE_API_BREVO_ICI` par votre clé API Brevo
- `votre-email@exemple.com` par l'email expéditeur vérifié dans Brevo
- `Electric Charge Platform` par le nom que vous souhaitez afficher

## 📋 Fonctionnalités implémentées

### 1. Inscription avec vérification d'email

Lors de l'inscription, un code à 6 chiffres est généré et envoyé par email.

**Endpoint:** `POST /api/auth/register`

**Réponse:**
```json
{
  "status": "SUCCESS",
  "message": "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.",
  "data": { ... }
}
```

### 2. Vérification de l'email

L'utilisateur entre le code reçu par email.

**Endpoint:** `POST /api/auth/verify-email`

**Corps de la requête:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Réponse réussie:**
```json
{
  "status": "SUCCESS",
  "message": "Email vérifié avec succès ! Vous pouvez maintenant vous connecter.",
  "data": null
}
```

### 3. Renvoyer le code de vérification

Si l'utilisateur n'a pas reçu le code ou s'il a expiré (15 minutes).

**Endpoint:** `POST /api/auth/resend-verification`

**Corps de la requête:**
```json
{
  "email": "user@example.com"
}
```

**Réponse:**
```json
{
  "status": "SUCCESS",
  "message": "Un nouveau code de vérification a été envoyé à votre adresse email.",
  "data": null
}
```

## 🎨 Emails envoyés

### Email de vérification
- **Objet:** "Validation de votre compte"
- **Contenu:** Code à 6 chiffres avec design moderne
- **Expiration:** 15 minutes

### Email de bienvenue
- **Objet:** "Bienvenue sur notre plateforme !"
- **Contenu:** Message de bienvenue et présentation des fonctionnalités
- **Envoyé:** Après la vérification réussie de l'email

## 🔒 Sécurité

- Les codes de vérification expirent après 15 minutes
- Un compte non vérifié ne peut pas se connecter
- Les codes sont stockés de manière sécurisée dans la base de données
- Un nouveau code invalide automatiquement l'ancien

## 🧪 Test de l'intégration

### Test manuel

1. Démarrez votre application
2. Inscrivez-vous avec une vraie adresse email
3. Vérifiez votre boîte de réception
4. Utilisez le code pour valider votre compte

### Vérifier les logs

Les logs indiquent si l'email a été envoyé avec succès :

```
INFO  c.e.a.s.i.EmailServiceImpl - Email de vérification envoyé à: user@example.com
```

En cas d'erreur :

```
ERROR c.e.a.s.i.EmailServiceImpl - Erreur lors de l'envoi de l'email de vérification à user@example.com: ...
```

## 📊 Limites du plan gratuit Brevo

- **300 emails/jour**
- Idéal pour le développement et les petites applications
- Pour la production avec plus d'utilisateurs, envisagez un plan payant

## 🔧 Alternatives gratuites

Si vous avez besoin de plus d'emails :

1. **SendGrid** : 100 emails/jour gratuit
2. **Mailgun** : 5000 emails/mois les 3 premiers mois
3. **Amazon SES** : 62,000 emails/mois (avec AWS Free Tier)

## 📝 Migration de base de données

Le champ `verification_code_expiry` a été ajouté au modèle `Utilisateur`. Assurez-vous de :

1. Recréer la base de données, ou
2. Ajouter manuellement la colonne :

```sql
ALTER TABLE utilisateur 
ADD COLUMN verification_code_expiry TIMESTAMP;
```

## ❓ Problèmes courants

### L'email n'arrive pas

1. Vérifiez que votre clé API est correcte
2. Vérifiez que l'email expéditeur est vérifié dans Brevo
3. Consultez les logs de l'application
4. Vérifiez le dossier spam

### Code invalide ou expiré

- Le code expire après 15 minutes
- Utilisez l'endpoint `/resend-verification` pour obtenir un nouveau code
- Un seul code est valide à la fois (le nouveau remplace l'ancien)

### Limite d'emails atteinte

- Brevo vous envoie une notification quand vous approchez de la limite
- Attendez le lendemain ou passez à un plan payant

## 🎯 Prochaines étapes

Pour améliorer le système d'emails, vous pouvez :

1. Ajouter la récupération de mot de passe par email
2. Créer des templates HTML personnalisés dans Brevo
3. Ajouter des notifications par email pour les réservations
4. Implémenter un système de newsletter
5. Ajouter des emails de rappel pour les réservations

## 📞 Support

- Documentation Brevo : [https://developers.brevo.com/](https://developers.brevo.com/)
- Support Brevo : [https://help.brevo.com/](https://help.brevo.com/)
