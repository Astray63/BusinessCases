# Espace Propriétaire - Documentation

## 📋 Vue d'ensemble

L'**Espace Propriétaire** est un module complet permettant aux propriétaires de bornes électriques de gérer leurs installations et les réservations associées.

## 🎯 Fonctionnalités

### 1. Dashboard Propriétaire (`/proprietaire`)
- **Statistiques en temps réel** :
  - Total des bornes enregistrées
  - Bornes actives vs inactives
  - Demandes de réservation en attente
  - Réservations confirmées
  - Taux d'occupation
  - Revenus du mois en cours
  - Revenus totaux

- **Vue d'ensemble** :
  - Demandes récentes nécessitant validation
  - Aperçu des bornes (6 premières)
  - Dernières réservations (5 dernières)
  - Actions rapides vers les autres sections

### 2. Gestion des Bornes (`/proprietaire/mes-bornes`)
- **Liste complète** des bornes du propriétaire
- **Ajout de nouvelle borne** :
  - Sélection du lieu
  - Description de la localisation
  - Type de prise (Type 2, CCS, CHAdeMO, etc.)
  - Puissance (kW)
  - Tarif horaire
  - État initial
  
- **Modification de borne existante** :
  - Mise à jour de tous les paramètres
  
- **Changement d'état** :
  - Disponible
  - En maintenance
  - Hors service
  
- **Suppression** de borne (si pas de réservations actives)

### 3. Demandes de Réservation (`/proprietaire/demandes`)
- **Liste des demandes EN_ATTENTE** uniquement
- **Informations détaillées** :
  - Identité du client
  - Borne concernée
  - Date et heure
  - Durée de réservation
  - Montant estimé
  
- **Actions** :
  - ✅ **Accepter** : La réservation passe à CONFIRMEE
  - ❌ **Refuser** : Possibilité d'ajouter un motif de refus

### 4. Historique des Réservations (`/proprietaire/historique`)
- **Tableau complet** de toutes les réservations
- **Filtres avancés** :
  - Par statut (En attente, Confirmée, Terminée, Annulée, Refusée)
  - Par borne
  - Par période (date début/fin)
  
- **Statistiques de la vue filtrée** :
  - Nombre total de réservations
  - Nombre de réservations validées
  - Revenus générés
  
- **Informations affichées** :
  - ID de réservation
  - Identité du client
  - Borne utilisée
  - Date et horaire
  - Durée
  - Statut
  - Montant

## 🔐 Contrôle d'Accès

- **Rôles autorisés** : `proprietaire` et `admin`
- **Protection par AuthGuard** : Redirection automatique si non autorisé
- **Visibilité dans le menu** : Le lien "Espace Propriétaire" n'apparaît que pour les utilisateurs autorisés

## 🎨 Design

- **Couleur principale** : Orange (#f57c00) pour différencier de l'interface client
- **Style cohérent** avec le reste de l'application
- **Responsive** : Adapté aux mobiles, tablettes et ordinateurs
- **Animations fluides** : Transitions et effets hover
- **Icônes** : Bootstrap Icons pour la cohérence visuelle

## 🛠️ Architecture Technique

### Structure des fichiers
```
frontend/src/app/pages/proprietaire/
├── proprietaire.module.ts
├── dashboard-proprietaire/
│   ├── dashboard-proprietaire.component.ts
│   ├── dashboard-proprietaire.component.html
│   └── dashboard-proprietaire.component.scss
├── mes-bornes/
│   ├── mes-bornes.component.ts
│   ├── mes-bornes.component.html
│   └── mes-bornes.component.scss
├── demandes-reservation/
│   ├── demandes-reservation.component.ts
│   ├── demandes-reservation.component.html
│   └── demandes-reservation.component.scss
└── historique-reservations/
    ├── historique-reservations.component.ts
    ├── historique-reservations.component.html
    └── historique-reservations.component.scss
```

### Routes
```typescript
/proprietaire               → Dashboard propriétaire
/proprietaire/mes-bornes    → Gestion des bornes
/proprietaire/demandes      → Demandes de réservation
/proprietaire/historique    → Historique complet
```

### Services utilisés
- **AuthService** : Authentification et récupération utilisateur
- **BorneService** : CRUD bornes + `getBornesByProprietaire()`
- **LieuService** : Gestion des lieux
- **ReservationService** : 
  - `getReservationsProprietaire()`
  - `accepterReservation()`
  - `refuserReservation()`

## 📊 Workflow Propriétaire

### 1. Première utilisation
```
Inscription/Connexion 
→ Dashboard 
→ Clic sur "Espace Propriétaire"
→ Ajout d'un lieu (/lieux)
→ Ajout d'une borne (/proprietaire/mes-bornes)
→ Attente de réservations
```

### 2. Gestion quotidienne
```
Notification email (nouvelle demande)
→ Espace Propriétaire
→ Onglet "Demandes"
→ Consultation des détails
→ Acceptation ou Refus
→ Notification automatique au client
```

### 3. Suivi et analyse
```
Espace Propriétaire
→ Dashboard : Vue d'ensemble
→ Historique : Analyse détaillée
→ Filtres : Périodes spécifiques
→ Statistiques : Revenus, taux d'occupation
```

## 🚀 Fonctionnalités Futures (Suggestions)

### Court terme
- [ ] Export Excel/PDF de l'historique
- [ ] Graphiques de statistiques (Chart.js)
- [ ] Notifications en temps réel (WebSocket)
- [ ] Calendrier de disponibilité

### Moyen terme
- [ ] Tarification dynamique (heures creuses/pleines)
- [ ] Promotions et réductions
- [ ] Système de notation des clients
- [ ] Messagerie interne avec les clients

### Long terme
- [ ] Application mobile dédiée
- [ ] Tableau de bord analytique avancé
- [ ] IA pour optimisation des tarifs
- [ ] Intégration systèmes domotiques

## 🐛 Résolution de Problèmes

### Les bornes n'apparaissent pas
- Vérifier que `ownerId` est bien défini dans les bornes
- Vérifier les permissions de l'utilisateur
- Consulter la console navigateur pour les erreurs API

### Les demandes ne s'affichent pas
- Vérifier que l'endpoint `/reservations/proprietaire/{id}` fonctionne
- S'assurer que les réservations ont le statut `EN_ATTENTE`
- Vérifier les relations entre Reservation, Borne et Utilisateur

### Erreur lors de l'acceptation/refus
- Vérifier que l'utilisateur est bien propriétaire de la borne
- Contrôler les permissions côté backend
- Consulter les logs du serveur

## 📝 API Backend Requise

### Endpoints nécessaires
```
GET    /api/bornes/proprietaire/{proprietaireId}
GET    /api/reservations/proprietaire/{proprietaireId}
PUT    /api/reservations/{id}/accepter
PUT    /api/reservations/{id}/refuser
POST   /api/bornes
PUT    /api/bornes/{id}
DELETE /api/bornes/{id}
```

### Modèle Borne
```java
public class Borne {
    private Long idBorne;
    private String localisation;
    private String type;
    private Double puissance;
    private Double prix;
    private String etat; // DISPONIBLE, OCCUPEE, HORS_SERVICE, EN_MAINTENANCE
    private Long ownerId; // ID du propriétaire
    private Lieu lieu;
    // ... autres champs
}
```

### Modèle Reservation
```java
public class Reservation {
    private Long idReservation;
    private Date dateDebut;
    private Date dateFin;
    private String statut; // EN_ATTENTE, CONFIRMEE, TERMINEE, ANNULEE, REFUSEE
    private Double montantTotal;
    private String motifRefus;
    private Utilisateur utilisateur;
    private Borne borne;
    // ... autres champs
}
```

## 🧪 Tests

### Tests Unitaires
- [ ] DashboardProprietaireComponent
- [ ] MesBornesComponent
- [ ] DemandesReservationComponent
- [ ] HistoriqueReservationsComponent

### Tests E2E
- [ ] Ajout d'une borne
- [ ] Modification d'une borne
- [ ] Acceptation d'une demande
- [ ] Refus d'une demande avec motif
- [ ] Filtrage de l'historique

### Tests d'intégration
- [ ] Flux complet propriétaire
- [ ] Synchronisation client-propriétaire
- [ ] Notifications email

## 📞 Support

Pour toute question ou problème :
1. Consulter cette documentation
2. Vérifier les logs navigateur (F12)
3. Consulter les logs serveur
4. Contacter l'équipe de développement

---

**Version** : 1.0.0  
**Date de création** : 15 novembre 2025  
**Dernière mise à jour** : 15 novembre 2025
