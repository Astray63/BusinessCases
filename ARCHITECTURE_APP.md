# 🏗️ Architecture de l'application "Electricity Business"

## 📋 Concept principal

**Un seul type de compte utilisateur** qui peut accéder à deux modes :
- 🔵 **Mode Client** : Rechercher et réserver des bornes
- 🟢 **Mode Propriétaire** : Gérer ses propres bornes et réservations

L'accès au mode propriétaire est **dynamique** : il s'active automatiquement dès qu'un utilisateur possède au moins une borne.

---

## 🗂️ Structure des dossiers

```
frontend/src/app/
├── pages/
│   ├── home/                          # Page d'accueil publique
│   ├── auth/                          # Authentification (login, register, verify)
│   ├── dashboard/                     # Dashboard unifié (point d'entrée après login)
│   ├── profile/                       # Profil utilisateur
│   │
│   ├── client/                        # 🔵 MODE CLIENT
│   │   ├── recherche-bornes/          # Recherche et carte des bornes
│   │   ├── recherche-lieux/           # Recherche des lieux
│   │   ├── reservation-create/        # Créer une réservation
│   │   ├── mes-reservations/          # Liste des réservations (en cours + passées)
│   │   └── client.module.ts           # Module lazy-loaded
│   │
│   ├── proprietaire/                  # 🟢 MODE PROPRIÉTAIRE
│   │   ├── dashboard-proprietaire/    # Vue d'ensemble propriétaire
│   │   ├── mes-lieux/                 # Gérer mes lieux de recharge
│   │   ├── mes-bornes/                # Gérer mes bornes
│   │   ├── demandes-reservation/      # Réservations reçues à accepter/refuser
│   │   ├── historique-reservations/   # Historique des réservations sur mes bornes
│   │   └── proprietaire.module.ts     # Module lazy-loaded
│   │
│   └── admin/                         # 🔧 ADMINISTRATION (rôle admin uniquement)
│
├── guards/
│   ├── auth.guard.ts                  # Vérifie que l'utilisateur est connecté
│   ├── proprietaire.guard.ts          # Vérifie que l'utilisateur possède ≥ 1 borne
│   └── admin.guard.ts                 # Vérifie le rôle admin
│
├── services/
│   ├── auth.service.ts                # Gestion de l'authentification
│   ├── user-context.service.ts        # Gestion du contexte utilisateur (isProprietaire, nombreBornes)
│   ├── borne.service.ts               # CRUD bornes
│   ├── lieu.service.ts                # CRUD lieux
│   ├── reservation.service.ts         # CRUD réservations
│   └── ...
│
├── models/
│   ├── utilisateur.model.ts           # Modèle utilisateur (avec isProprietaire dynamique)
│   ├── borne.model.ts
│   ├── lieu.model.ts
│   ├── reservation.model.ts
│   └── ...
│
└── components/
    ├── header/                        # Header avec navigation contextuelle
    ├── footer/
    └── shared/                        # Composants réutilisables
```

---

## 🛣️ Organisation des routes

### Routes publiques
```
/home                           → Page d'accueil
/auth/login                     → Connexion
/auth/register                  → Inscription
/auth/verify-email              → Vérification email
```

### Routes protégées (nécessite authentification)
```
/dashboard                      → Dashboard unifié (affiche mode client + propriétaire si applicable)
/profile                        → Profil utilisateur
```

### 🔵 Routes MODE CLIENT (nécessite authentification)
```
/client/recherche               → Recherche et carte des bornes disponibles
/client/lieux                   → Recherche des lieux de recharge
/client/reservation/create      → Créer une réservation
/client/mes-reservations        → Liste de mes réservations (client)
```

### 🟢 Routes MODE PROPRIÉTAIRE (nécessite authentification + posséder ≥ 1 borne)
```
/proprietaire/dashboard         → Vue d'ensemble propriétaire (stats, revenus)
/proprietaire/mes-lieux         → Gérer mes lieux (CRUD)
/proprietaire/mes-bornes        → Gérer mes bornes (CRUD)
/proprietaire/demandes          → Demandes de réservation à traiter
/proprietaire/historique        → Historique réservations sur mes bornes
```

### 🔧 Routes ADMIN (nécessite rôle admin)
```
/admin/utilisateurs             → Gestion utilisateurs
/admin/bornes                   → Gestion globale des bornes
/admin/statistiques             → Statistiques globales
```

---

## 🔐 Gestion des accès (Guards)

### 1. **AuthGuard**
- Vérifie que l'utilisateur est connecté
- Utilisé sur toutes les routes protégées

### 2. **ProprietaireGuard**
- Vérifie que l'utilisateur est connecté **ET** possède au moins 1 borne
- Utilisé sur toutes les routes `/proprietaire/*`
- Si l'utilisateur n'a pas de borne → redirection vers `/dashboard` avec message

### 3. **AdminGuard**
- Vérifie que l'utilisateur a le rôle `admin`
- Utilisé sur toutes les routes `/admin/*`

---

## 🎯 Service UserContextService

Ce service est **central** pour gérer le contexte utilisateur :

```typescript
class UserContextService {
  // Observables
  isProprietaire$: Observable<boolean>
  nombreBornes$: Observable<number>
  
  // Méthodes synchrones
  isCurrentUserProprietaire(): boolean
  getCurrentNombreBornes(): number
  
  // Rafraîchir le statut
  refreshProprietaireStatus(): void
}
```

**Fonctionnement** :
1. S'abonne à `authService.currentUser$`
2. Dès qu'un utilisateur se connecte, appelle `borneService.getBornesByProprietaire()`
3. Met à jour `isProprietaire$` et `nombreBornes$`
4. Ces observables sont utilisés dans :
   - Le header (pour afficher/masquer les menus)
   - Le dashboard (pour afficher/masquer les sections)
   - Le guard proprietaire

---

## 🧭 Navigation dans le Header

Le header doit s'adapter au contexte utilisateur :

### Utilisateur NON connecté
```
🏠 Accueil | 🔋 Bornes | 🔐 Connexion | ✍️ Inscription
```

### Utilisateur connecté (pas de borne)
```
🏠 Accueil | 🔍 Rechercher | 📅 Mes Réservations | 👤 Profil | 🚪 Déconnexion
```

### Utilisateur connecté + Propriétaire (≥ 1 borne)
```
🏠 Accueil | 
🔵 CLIENT: 🔍 Rechercher | 📅 Mes Réservations | 
🟢 PROPRIÉTAIRE: 🏢 Mes Bornes | 📬 Demandes | 
👤 Profil | 🚪 Déconnexion
```

**Implémentation** :
```typescript
// header.component.ts
isProprietaire$ = this.userContextService.isProprietaire$;
nombreBornes$ = this.userContextService.nombreBornes$;

// header.component.html
<ng-container *ngIf="isProprietaire$ | async">
  <!-- Menu propriétaire -->
</ng-container>
```

---

## 📊 Dashboard unifié

Le dashboard est le **point d'entrée** après connexion. Il affiche :

### Section toujours visible (Mode Client)
- Statistiques personnelles (nombre de réservations, prochaine réservation)
- Accès rapides : "Rechercher une borne", "Mes réservations"

### Section conditionnelle (Mode Propriétaire) - SI `isProprietaire === true`
- Statistiques propriétaire (nombre de bornes, revenus, taux d'occupation)
- Accès rapides : "Mes bornes", "Demandes à traiter", "Ajouter une borne"

**Implémentation** :
```typescript
// dashboard.component.ts
isProprietaire$ = this.userContextService.isProprietaire$;

// dashboard.component.html
<div class="client-section">
  <!-- Toujours visible -->
</div>

<div class="proprietaire-section" *ngIf="isProprietaire$ | async">
  <!-- Visible uniquement si propriétaire -->
</div>
```

---

## 🔄 Flux utilisateur : Devenir propriétaire

### Scénario : Un utilisateur simple veut mettre sa borne en location

1. L'utilisateur va sur `/dashboard` ou `/profile`
2. Il voit un bouton **"Devenir propriétaire"** ou **"Ajouter ma première borne"**
3. Il clique → Redirection vers `/proprietaire/mes-bornes/ajouter`
4. **Problème** : Le guard `ProprietaireGuard` bloque l'accès (pas encore propriétaire)

### Solution : Route spéciale pour la première borne
```typescript
// app-routing.module.ts
{
  path: 'devenir-proprietaire',
  loadChildren: () => import('./pages/onboarding-proprietaire/onboarding.module'),
  canActivate: [AuthGuard]  // Pas de ProprietaireGuard !
}
```

Cette route :
- Explique le processus
- Permet d'ajouter lieu + première borne
- Une fois la borne créée, `UserContextService` se rafraîchit automatiquement
- L'utilisateur peut alors accéder aux routes `/proprietaire/*`

---

## 📝 Gestion des réservations : Deux perspectives

### 🔵 Mode CLIENT (`/client/mes-reservations`)
- Liste des réservations **faites par moi** (sur des bornes d'autres propriétaires)
- Actions : Annuler, Voir détails

### 🟢 Mode PROPRIÉTAIRE (`/proprietaire/demandes` et `/proprietaire/historique`)
- **Demandes** : Réservations **reçues sur mes bornes** en attente d'acceptation
- Actions : Accepter, Refuser
- **Historique** : Réservations passées sur mes bornes

**Important** : Deux endpoints API différents
```typescript
// reservation.service.ts

// Pour le mode client
getMesReservations(idUtilisateur: number): Observable<Reservation[]>

// Pour le mode propriétaire
getReservationsSurMesBornes(idUtilisateur: number): Observable<Reservation[]>
getDemandesEnAttente(idUtilisateur: number): Observable<Reservation[]>
```

---

## 🗑️ Suppression de borne

**Règle métier** : Une borne ne peut être supprimée que si elle n'a **aucune réservation passée**.

**Implémentation** :
```typescript
// borne.service.ts
deleteBorne(idBorne: number): Observable<any> {
  // Le backend vérifie les réservations
  // Renvoie une erreur si réservations existantes
}

// mes-bornes.component.ts
onDeleteBorne(borne: Borne) {
  if (confirm('Êtes-vous sûr de vouloir supprimer cette borne ?')) {
    this.borneService.deleteBorne(borne.idBorne).subscribe({
      next: () => {
        this.toastService.success('Borne supprimée');
        this.userContextService.refreshProprietaireStatus(); // Rafraîchir le statut
        this.loadBornes();
      },
      error: (err) => {
        if (err.status === 409) {
          this.toastService.error('Impossible de supprimer : des réservations existent');
        } else {
          this.toastService.error('Erreur lors de la suppression');
        }
      }
    });
  }
}
```

---

## ✅ Checklist d'implémentation

### Phase 1 : Réorganisation des routes
- [ ] Créer le module `client.module.ts` avec sous-routes
- [ ] Déplacer les pages existantes dans `/client/`
- [ ] Mettre à jour `app-routing.module.ts` avec la nouvelle structure
- [ ] Tester la navigation

### Phase 2 : Mode Propriétaire
- [ ] S'assurer que `proprietaire.module.ts` est bien organisé
- [ ] Vérifier que `ProprietaireGuard` fonctionne correctement
- [ ] Créer la page "Devenir propriétaire" (onboarding)
- [ ] Tester l'ajout de la première borne

### Phase 3 : Dashboard unifié
- [ ] Afficher section client (toujours visible)
- [ ] Afficher section propriétaire (conditionnelle avec `*ngIf="isProprietaire$ | async"`)
- [ ] Ajouter statistiques pertinentes

### Phase 4 : Header contextuel
- [ ] Implémenter la navigation adaptative selon `isProprietaire$`
- [ ] Tester l'affichage pour utilisateur simple vs propriétaire

### Phase 5 : Gestion des réservations
- [ ] Séparer les vues client et propriétaire
- [ ] Créer les endpoints API distincts
- [ ] Tester les deux perspectives

### Phase 6 : Suppression de bornes
- [ ] Ajouter la validation backend (pas de suppression si réservations)
- [ ] Gérer les erreurs côté frontend
- [ ] Rafraîchir le statut utilisateur après suppression

---

## 🎨 Recommandations UX

1. **Badge "Propriétaire"** dans le profil si `isProprietaire === true`
2. **Call-to-action** dans le dashboard pour devenir propriétaire
3. **Notifications** quand une demande de réservation est reçue
4. **Statistiques visuelles** dans le dashboard propriétaire (graphiques)
5. **Tutoriel** la première fois qu'un utilisateur accède au mode propriétaire

---

## 🔧 Services clés

| Service | Responsabilité |
|---------|---------------|
| `AuthService` | Authentification, gestion du token, utilisateur courant |
| `UserContextService` | Statut propriétaire dynamique, nombre de bornes |
| `BorneService` | CRUD bornes, récupération par propriétaire |
| `LieuService` | CRUD lieux |
| `ReservationService` | CRUD réservations (client + propriétaire) |

---

## 📦 Modules lazy-loaded

```typescript
// app-routing.module.ts
{
  path: 'client',
  loadChildren: () => import('./pages/client/client.module').then(m => m.ClientModule),
  canActivate: [AuthGuard]
},
{
  path: 'proprietaire',
  loadChildren: () => import('./pages/proprietaire/proprietaire.module').then(m => m.ProprietaireModule),
  canActivate: [AuthGuard, ProprietaireGuard]
}
```

**Avantages** :
- Performance : charge uniquement ce qui est nécessaire
- Séparation claire des responsabilités
- Scalabilité : facile d'ajouter de nouvelles fonctionnalités

---

## 🚀 Évolutions futures possibles

1. **Mode "Multi-propriétaire"** : Gérer plusieurs lieux/bornes avec filtres
2. **Statistiques avancées** : Revenus mensuels, taux d'occupation
3. **Notifications push** : Nouvelle réservation, rappels
4. **Tarification dynamique** : Selon l'heure, la demande
5. **Système de notation** : Les clients notent les bornes, les propriétaires notent les clients

---

Cette architecture est **propre**, **scalable** et **maintenable**. Elle respecte vos besoins fonctionnels tout en gardant un seul type de compte utilisateur. 🎉
