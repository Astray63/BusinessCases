# Configuration Sécurisée

## ⚠️ Important: Configuration des Secrets

Les clés API et informations sensibles ne doivent **JAMAIS** être commitées dans Git.

## Configuration Locale

### 1. Copier le fichier d'exemple
```bash
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
```

### 2. Configurer les variables d'environnement

Créez un fichier `.env` à la racine du projet (déjà dans .gitignore):

```bash
cp .env.example .env
```

Puis éditez `.env` avec vos vraies valeurs:
```properties
BREVO_API_KEY=votre_vraie_clé_api_brevo
BREVO_SENDER_EMAIL=votre-email@example.com
BREVO_SENDER_NAME=Votre Nom
```

### 3. Mettre à jour application.properties

Dans `backend/src/main/resources/application.properties`, les valeurs utilisent maintenant des variables d'environnement:

```properties
brevo.api.key=${BREVO_API_KEY}
brevo.sender.email=${BREVO_SENDER_EMAIL:astray63000@gmail.com}
brevo.sender.name=${BREVO_SENDER_NAME:Elijah Lasserre}
```

### 4. Lancer l'application avec les variables d'environnement

#### Option 1: Avec un fichier .env (nécessite un outil comme `dotenv`)
```bash
# Installer dotenv CLI
npm install -g dotenv-cli

# Lancer l'application
cd backend
dotenv -e ../.env mvn spring-boot:run
```

#### Option 2: Exporter les variables directement
```bash
export BREVO_API_KEY="votre_clé_api"
export BREVO_SENDER_EMAIL="votre-email@example.com"
export BREVO_SENDER_NAME="Votre Nom"
cd backend
mvn spring-boot:run
```

#### Option 3: IDE (IntelliJ IDEA, Eclipse, VS Code)
Configurez les variables d'environnement dans votre configuration de lancement.

## ⚠️ La clé API a été exposée

Si vous avez accidentellement commit et push une clé API (comme c'était le cas):

1. **Révoquez immédiatement la clé exposée** sur Brevo:
   - Allez sur https://app.brevo.com/settings/keys/api
   - Supprimez l'ancienne clé
   - Créez une nouvelle clé

2. **Mettez à jour votre configuration locale** avec la nouvelle clé

3. Les fichiers ont déjà été nettoyés du dépôt Git

## Fichiers à ne jamais committer

Ces fichiers sont maintenant dans `.gitignore`:
- `.env`
- `.env.local`
- `backend/src/main/resources/application.properties` (le fichier avec vos vraies valeurs)

## Fichiers à committer

Ces fichiers DOIVENT être committés (ils ne contiennent pas de secrets):
- `.env.example`
- `backend/src/main/resources/application.properties.example`
- `.gitignore`
