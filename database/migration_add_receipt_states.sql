-- ========================================
-- Migration: Ajout des nouveaux états de réservation et du champ receipt_path
-- Date: 17 novembre 2025
-- Description: Support pour les réservations en attente, confirmées et refusées avec génération de reçus PDF
-- ========================================

-- 1. Vérifier si la colonne receipt_path existe, sinon l'ajouter
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'reservation' AND column_name = 'receipt_path'
    ) THEN
        ALTER TABLE reservation ADD COLUMN receipt_path VARCHAR(255);
        RAISE NOTICE 'Colonne receipt_path ajoutée à la table reservation';
    ELSE
        RAISE NOTICE 'Colonne receipt_path existe déjà';
    END IF;
END $$;

-- 2. Supprimer l'ancienne contrainte sur l'état
ALTER TABLE reservation DROP CONSTRAINT IF EXISTS chk_etat_res;

-- 3. Ajouter la nouvelle contrainte avec les nouveaux états
ALTER TABLE reservation ADD CONSTRAINT chk_etat_res 
    CHECK (etat IN ('EN_ATTENTE','CONFIRMEE','ACTIVE','TERMINEE','ANNULEE','REFUSEE'));

-- 4. Mettre à jour les réservations existantes avec l'état ACTIVE vers CONFIRMEE
-- (optionnel selon votre logique métier)
-- UPDATE reservation SET etat = 'CONFIRMEE' WHERE etat = 'ACTIVE';

-- 5. Changer la valeur par défaut de l'état à EN_ATTENTE
ALTER TABLE reservation ALTER COLUMN etat SET DEFAULT 'EN_ATTENTE';

-- 6. Vérification
DO $$
DECLARE
    constraint_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.constraint_column_usage 
        WHERE constraint_name = 'chk_etat_res'
    ) INTO constraint_exists;
    
    IF constraint_exists THEN
        RAISE NOTICE 'Migration réussie : Contrainte chk_etat_res mise à jour avec les nouveaux états';
    ELSE
        RAISE EXCEPTION 'Erreur : La contrainte chk_etat_res n''a pas été créée';
    END IF;
END $$;

-- 7. Afficher les états actuels des réservations
SELECT etat, COUNT(*) as nombre 
FROM reservation 
GROUP BY etat 
ORDER BY nombre DESC;

RAISE NOTICE 'Migration terminée avec succès !';
