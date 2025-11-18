# Correction du Workflow de Réservation

## Problème Identifié

Le système réservait et bloquait immédiatement la plage horaire d'une borne **avant** que la réservation soit confirmée/payée, ce qui n'est pas logique. Une réservation en état `EN_ATTENTE` ne devrait pas empêcher d'autres utilisateurs de réserver la même plage.

## Solution Implémentée

### 1. Modification de la Logique de Conflit (✅ Critique)

**Fichier**: `ReservationRepository.java`

La méthode `findConflictingReservations` a été modifiée pour ne considérer que les réservations **confirmées, actives ou terminées** comme bloquantes :

```java
@Query("""
    SELECT r FROM Reservation r 
    WHERE r.chargingStation.idBorne = :idBorne 
    AND r.etat IN ('CONFIRMEE', 'ACTIVE', 'TERMINEE')
    AND ((r.dateDebut BETWEEN :dateDebut AND :dateFin) 
    OR (r.dateFin BETWEEN :dateDebut AND :dateFin)
    OR (:dateDebut BETWEEN r.dateDebut AND r.dateFin))
    """)
```

**Impact** :
- ✅ Les réservations `EN_ATTENTE` ne bloquent plus les créneaux
- ✅ Plusieurs utilisateurs peuvent créer des réservations en attente sur la même plage
- ✅ Seule la première réservation **confirmée** par le propriétaire bloque le créneau

### 2. Validation lors de l'Acceptation (✅ Sécurité)

**Fichier**: `ReservationServiceImpl.java` - méthode `accepter()`

Ajout d'une vérification pour s'assurer qu'au moment de la confirmation, la plage horaire est toujours disponible :

```java
// Vérifier qu'il n'y a pas de conflit avec d'autres réservations confirmées
var conflicts = reservationRepository.findConflictingReservations(
        reservation.getChargingStation().getIdBorne(), 
        reservation.getDateDebut(), 
        reservation.getDateFin());
if (!conflicts.isEmpty()) {
    throw new IllegalArgumentException("Conflit de réservation : la plage horaire est maintenant occupée");
}
```

**Impact** :
- ✅ Le propriétaire ne peut confirmer qu'une seule réservation par plage horaire
- ✅ Si le propriétaire confirme la réservation A, les réservations B et C en attente sur la même plage ne pourront plus être confirmées

### 3. Nettoyage Automatique des Réservations (✅ Maintenance)

**Fichier**: `ReservationCleanupService.java` (nouveau)

Service automatique qui nettoie les réservations en attente :

#### Nettoyage 1 : Réservations Anciennes
- **Fréquence** : Toutes les heures
- **Action** : Annule les réservations en `EN_ATTENTE` depuis plus de 24h
- **Raison** : Éviter l'accumulation de réservations oubliées

#### Nettoyage 2 : Réservations Datées
- **Fréquence** : Toutes les 30 minutes
- **Action** : Annule les réservations en `EN_ATTENTE` dont la date de début est passée
- **Raison** : Une réservation dont la date est passée n'a plus de sens

**Activation** : Ajout de `@EnableScheduling` dans `ElectricityBusinessApplication.java`

### 4. Correction du Lazy Loading (✅ Bug Fix)

**Fichier**: `ReservationRepository.java`

Ajout de `LEFT JOIN FETCH cs.owner` dans toutes les requêtes pour éviter les erreurs de lazy loading lors de la sérialisation JSON :

```java
@Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.chargingStation cs LEFT JOIN FETCH cs.owner LEFT JOIN FETCH cs.medias WHERE r.numeroReservation = :id")
```

## Workflow Actuel

### Scénario Normal

1. **Client A** : Crée une réservation → État `EN_ATTENTE` (ne bloque PAS la plage)
2. **Client B** : Peut aussi créer une réservation sur la même plage → État `EN_ATTENTE`
3. **Propriétaire** : Accepte la réservation du Client A → État `CONFIRMEE` (bloque maintenant la plage)
4. **Propriétaire** : Tente d'accepter la réservation du Client B → ❌ ERREUR "Conflit de réservation"

### Scénario avec Expiration

1. **Client** : Crée une réservation → État `EN_ATTENTE`
2. **Temps** : 24 heures passent sans confirmation
3. **Système** : Annule automatiquement → État `ANNULEE`

### Scénario avec Date Passée

1. **Client** : Crée une réservation pour demain à 10h → État `EN_ATTENTE`
2. **Temps** : Demain 10h arrive, toujours pas confirmée
3. **Système** : Annule automatiquement → État `ANNULEE`

## États de Réservation

| État | Description | Bloque la plage ? |
|------|-------------|-------------------|
| `EN_ATTENTE` | En attente de validation du propriétaire | ❌ NON |
| `CONFIRMEE` | Acceptée et payée | ✅ OUI |
| `ACTIVE` | En cours d'utilisation | ✅ OUI |
| `TERMINEE` | Session terminée | ✅ OUI (historique) |
| `ANNULEE` | Annulée (client ou expiration) | ❌ NON |
| `REFUSEE` | Refusée par le propriétaire | ❌ NON |

## Configuration Optionnelle

Si vous souhaitez modifier la durée d'expiration des réservations en attente, éditez :

**Fichier** : `ReservationCleanupService.java`
```java
private static final int EXPIRATION_HOURS = 24; // Changer cette valeur
```

## Tests Recommandés

1. ✅ Créer plusieurs réservations EN_ATTENTE sur la même plage
2. ✅ Confirmer l'une d'elles
3. ✅ Vérifier que les autres ne peuvent plus être confirmées
4. ✅ Vérifier que les réservations anciennes sont annulées automatiquement
5. ✅ Vérifier que les réservations avec date passée sont annulées

## Bénéfices

- ✅ Workflow logique : réservation → validation → blocage
- ✅ Plusieurs demandes possibles sur la même plage
- ✅ Le propriétaire choisit quelle réservation accepter
- ✅ Pas d'accumulation de réservations oubliées
- ✅ Pas de blocage de plages par des réservations non confirmées
