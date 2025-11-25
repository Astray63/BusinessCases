-- Migration script to fix schema issues
-- Run this script on your existing database

-- Step 1: Drop the view that depends on hourly_rate
DROP VIEW IF EXISTS v_borne_complete CASCADE;

-- Step 2: Alter hourly_rate column type (if needed)
-- This step is optional since we've fixed the annotation in the entity
-- ALTER TABLE charging_stations ALTER COLUMN hourly_rate TYPE DECIMAL(10,2);

-- Step 3: Add missing columns to utilisateur table
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS telephone VARCHAR(20);
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS code_postal VARCHAR(10);
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS ville VARCHAR(100);

-- Step 4: Recreate the view
CREATE OR REPLACE VIEW v_borne_complete AS
SELECT 
    b.*,
    l.adresse,
    l.ville,
    l.code_postal,
    l.pays,
    u.nom AS proprietaire_nom,
    u.prenom AS proprietaire_prenom
FROM charging_stations b
LEFT JOIN charging_station_lieu bl ON b.id_borne = bl.borne_id
LEFT JOIN lieu l ON bl.lieu_id = l.id_lieu
LEFT JOIN utilisateur u ON b.owner_id = u.id_utilisateur;
