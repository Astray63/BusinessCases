# Fonctionnalité : Reçu PDF pour Réservations Acceptées

## 📋 Description

Cette fonctionnalité permet de générer automatiquement un reçu PDF professionnel pour chaque réservation acceptée par un propriétaire de borne. Le PDF contient toutes les informations détaillées de la réservation et peut être téléchargé par les utilisateurs depuis l'interface web.

## 🎯 Fonctionnement

### 1. Génération automatique du PDF

Lorsqu'un propriétaire accepte une réservation :
- Le système change le statut de la réservation de `EN_ATTENTE` à `CONFIRMEE`
- Un reçu PDF est automatiquement généré et stocké sur le serveur
- Le chemin du PDF est enregistré dans la base de données (champ `receipt_path`)

### 2. Téléchargement du PDF

Les utilisateurs peuvent télécharger leur reçu PDF depuis :
- L'onglet "En cours" pour les réservations confirmées actives
- L'onglet "Passées" pour les réservations terminées ou confirmées
- L'onglet propriétaire "Mes bornes" pour visualiser les reçus des réservations confirmées

## 🏗️ Architecture Technique

### Backend (Java Spring Boot)

#### Services créés :
1. **PdfReceiptService** (`com.electriccharge.app.service.PdfReceiptService`)
   - Interface définissant les méthodes de génération et récupération de PDF

2. **PdfReceiptServiceImpl** (`com.electriccharge.app.service.impl.PdfReceiptServiceImpl`)
   - Implémentation utilisant iText 5.5.13.3
   - Génère des PDF avec logo, informations client, détails de la borne et de la réservation
   - Stocke les PDF dans le répertoire configuré

#### Endpoints REST :
- `PUT /api/reservations/{id}/accepter` : Accepte une réservation et génère le PDF
- `PUT /api/reservations/{id}/refuser` : Refuse une réservation
- `GET /api/reservations/{id}/receipt` : Télécharge le PDF d'une réservation

#### Modèle de données :
- Ajout du champ `receiptPath` dans `ReservationDto`
- Nouveaux états dans `Reservation.EtatReservation` :
  - `EN_ATTENTE` : État par défaut d'une nouvelle réservation
  - `CONFIRMEE` : Réservation acceptée par le propriétaire
  - `REFUSEE` : Réservation refusée par le propriétaire

### Frontend (Angular)

#### Services modifiés :
- **ReservationService** : Ajout de la méthode `downloadReceipt(id: number)` pour télécharger le PDF

#### Composants modifiés :
- **ReservationComponent** : 
  - Ajout de la méthode `telechargerRecu(reservationId: number)`
  - Boutons "Reçu PDF" visibles uniquement pour les réservations confirmées

#### Interface utilisateur :
- Bouton bleu "Reçu PDF" avec icône 📄
- Utilisation de FileSaver.js pour le téléchargement côté client
- Affichage conditionnel selon le statut de la réservation

## 📦 Dépendances

### Backend
```xml
<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

### Frontend
```json
{
  "file-saver": "^2.x.x"
}
```

## ⚙️ Configuration

### application.properties
```properties
# Répertoire de stockage des reçus PDF
app.receipts.storage.path=./storage/receipts
```

Le répertoire est créé automatiquement s'il n'existe pas.

## 🎨 Contenu du PDF

Le reçu PDF généré contient :

### En-tête
- Logo "ElectricCharge"
- Titre "Reçu de réservation"

### Informations client
- Nom et prénom
- Email
- Pseudo (si disponible)

### Informations de la borne
- Nom de la borne
- Localisation
- Numéro
- Puissance (kW)
- Type de connecteur

### Détails de la réservation
- Numéro de réservation
- Date et heure de début
- Date et heure de fin
- Prix à la minute
- Statut
- **Montant total** (en gros)

### Pied de page
- Message de remerciement
- Contact support

## 🔒 Sécurité

- Seul le propriétaire de la borne peut accepter/refuser une réservation
- Validation de l'identité du propriétaire avant génération du PDF
- Les réservations doivent être en statut `EN_ATTENTE` pour être acceptées/refusées

## 🧪 Tests

### Scénario de test :
1. Créer une réservation (statut `EN_ATTENTE`)
2. Accepter la réservation en tant que propriétaire
3. Vérifier que le statut passe à `CONFIRMEE`
4. Vérifier qu'un fichier PDF est créé dans `./storage/receipts/`
5. Télécharger le reçu depuis l'interface utilisateur
6. Vérifier le contenu du PDF

## 📝 Workflow complet

```
1. Client crée une réservation
   ↓
2. Statut = EN_ATTENTE
   ↓
3. Propriétaire reçoit la demande
   ↓
4. Propriétaire accepte
   ↓
5. Statut = CONFIRMEE
   ↓
6. PDF généré automatiquement
   ↓
7. Client peut télécharger le reçu
```

## 🐛 Gestion des erreurs

- Si la génération du PDF échoue, la réservation est quand même acceptée (statut CONFIRMEE)
- Un log d'erreur est enregistré pour investigation
- Message d'erreur affiché à l'utilisateur si le téléchargement échoue

## 🚀 Améliorations futures possibles

1. Envoi automatique du PDF par email lors de l'acceptation
2. Génération de factures mensuelles pour les propriétaires
3. Personnalisation du logo et des couleurs du PDF
4. Support multilingue des reçus
5. Code QR pour vérification du reçu
6. Statistiques sur les reçus générés

## 📊 Impact sur la base de données

- Aucune migration nécessaire si le champ `receipt_path` existe déjà
- Si non existant, ajouter : `ALTER TABLE reservation ADD COLUMN receipt_path VARCHAR(500);`

## 🔗 Fichiers modifiés

### Backend
- `backend/src/main/java/com/electriccharge/app/service/PdfReceiptService.java` (nouveau)
- `backend/src/main/java/com/electriccharge/app/service/impl/PdfReceiptServiceImpl.java` (nouveau)
- `backend/src/main/java/com/electriccharge/app/service/ReservationService.java`
- `backend/src/main/java/com/electriccharge/app/service/impl/ReservationServiceImpl.java`
- `backend/src/main/java/com/electriccharge/app/controller/ReservationController.java`
- `backend/src/main/java/com/electriccharge/app/model/Reservation.java`
- `backend/src/main/java/com/electriccharge/app/dto/ReservationDto.java`
- `backend/src/main/resources/application.properties`

### Frontend
- `frontend/src/app/services/reservation.service.ts`
- `frontend/src/app/pages/reservation/reservation.component.ts`
- `frontend/src/app/pages/reservation/reservation.component.html`

## ✅ Statut

**Feature complètement implémentée et fonctionnelle** ✨

Date de mise en œuvre : 17 novembre 2025
