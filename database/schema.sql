-- ========================================
-- Database: electricity_business
-- IMPORTANT: Script entièrement robuste et ré-exécutable
-- ========================================

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- ========================================
-- DROP ROBUST DES VUES / TABLES / MVIEW QUI PORTENT LES NOMS
-- ========================================

-- Drop tables first (in case these objects are tables)
DROP TABLE IF EXISTS v_borne_complete CASCADE;
DROP TABLE IF EXISTS v_reservation_complete CASCADE;

-- Drop materialized views if they exist
DROP MATERIALIZED VIEW IF EXISTS v_borne_complete CASCADE;
DROP MATERIALIZED VIEW IF EXISTS v_reservation_complete CASCADE;

-- Drop standard views
DROP VIEW IF EXISTS v_borne_complete CASCADE;
DROP VIEW IF EXISTS v_reservation_complete CASCADE;

-- ========================================
-- DROP EVERYTHING ELSE
-- ========================================
DROP TABLE IF EXISTS signalement CASCADE;
DROP TABLE IF EXISTS avis CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS charging_station_lieu CASCADE;
DROP TABLE IF EXISTS utilisateur_lieu CASCADE;
DROP TABLE IF EXISTS borne_medias CASCADE;
DROP TABLE IF EXISTS utilisateur_medias CASCADE;
DROP TABLE IF EXISTS charging_stations CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS lieu CASCADE;

-- ========================================
-- Table: lieu
-- ========================================
CREATE TABLE lieu (
    id_lieu BIGSERIAL PRIMARY KEY,
    adresse VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    numero VARCHAR(20),
    rue VARCHAR(255),
    code_postal VARCHAR(20) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    pays VARCHAR(100) NOT NULL DEFAULT 'France',
    region VARCHAR(100),
    complement_etape TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT AddGeometryColumn('lieu', 'geom', 4326, 'POINT', 2);
UPDATE lieu SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE longitude IS NOT NULL AND latitude IS NOT NULL;
CREATE INDEX idx_lieu_geom ON lieu USING GIST (geom);

-- ========================================
-- Table: utilisateur
-- ========================================
CREATE TABLE utilisateur (
    id_utilisateur BIGSERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    pseudo VARCHAR(50) UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'client',
    email VARCHAR(100) NOT NULL UNIQUE,
    adresse_physique VARCHAR(255),
    date_naissance DATE,
    age INTEGER,
    est_banni BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(64),
    verification_code_expiry TIMESTAMP,
    iban VARCHAR(34),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_role CHECK (role IN ('client','proprietaire','admin')),
    CONSTRAINT chk_age CHECK (age >= 0 AND age <= 120)
);

CREATE TABLE utilisateur_medias (
    utilisateur_id BIGINT NOT NULL,
    medias TEXT NOT NULL,
    CONSTRAINT fk_utilisateur_medias
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

-- ========================================
-- Table: charging_stations
-- ========================================
CREATE TABLE charging_stations (
    id_borne BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    localisation VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    puissance INTEGER NOT NULL,
    instruction_sur_pied TEXT,
    etat VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    occupee BOOLEAN DEFAULT FALSE,
    prix_a_la_minute DECIMAL(10,4) NOT NULL,
    hourly_rate DECIMAL(10,2),
    connector_type VARCHAR(50) NOT NULL,
    description TEXT,
    power_output DOUBLE PRECISION,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_etat CHECK (etat IN ('DISPONIBLE','OCCUPEE','EN_PANNE','EN_MAINTENANCE')),
    CONSTRAINT chk_puissance CHECK (puissance > 0),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE borne_medias (
    borne_id BIGINT NOT NULL,
    media_url TEXT NOT NULL,
    CONSTRAINT fk_borne_medias
        FOREIGN KEY (borne_id) REFERENCES charging_stations(id_borne) ON DELETE CASCADE
);

SELECT AddGeometryColumn('charging_stations', 'geom', 4326, 'POINT', 2);
CREATE INDEX idx_charging_stations_geom ON charging_stations USING GIST (geom);

-- ========================================
-- Table: vehicule
-- ========================================
CREATE TABLE vehicule (
    plaque_immatriculation VARCHAR(20) PRIMARY KEY,
    marque VARCHAR(50) NOT NULL,
    modele VARCHAR(50) NOT NULL,
    annee INTEGER,
    capacite_batterie INTEGER,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_annee CHECK (annee >= 1900 AND annee <= EXTRACT(YEAR FROM CURRENT_DATE) + 1),
    CONSTRAINT fk_vehicule_user FOREIGN KEY (user_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

-- ========================================
-- Table: reservation
-- ========================================
CREATE TABLE reservation (
    numero_reservation BIGSERIAL PRIMARY KEY,
    id_utilisateur BIGINT NOT NULL,
    charging_station_id BIGINT NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    prix_a_la_minute DECIMAL(10,4) NOT NULL,
    etat VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_price DECIMAL(10,2),
    receipt_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_dates CHECK (date_fin > date_debut),
    CONSTRAINT chk_etat_res CHECK (etat IN ('ACTIVE','TERMINEE','ANNULEE')),
    CONSTRAINT fk_res_user FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_res_borne FOREIGN KEY (charging_station_id) REFERENCES charging_stations(id_borne) ON DELETE CASCADE
);

-- ========================================
-- Table: avis
-- ========================================
CREATE TABLE avis (
    id_avis BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    charging_station_id BIGINT,
    note INTEGER NOT NULL,
    commentaire TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_note CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT fk_avis_user FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_avis_borne FOREIGN KEY (charging_station_id) REFERENCES charging_stations(id_borne) ON DELETE SET NULL
);

-- ========================================
-- Table: signalement
-- ========================================
CREATE TABLE signalement (
    id_signalement BIGSERIAL PRIMARY KEY,
    date_signalement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT NOT NULL,
    statut VARCHAR(20) DEFAULT 'OUVERT',
    date_resolution TIMESTAMP,
    user_id BIGINT NOT NULL,
    charging_station_id BIGINT,
    reservation_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_statut CHECK (statut IN ('OUVERT','EN_COURS','RESOLU','FERME')),
    CONSTRAINT fk_sig_user FOREIGN KEY (user_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_sig_borne FOREIGN KEY (charging_station_id) REFERENCES charging_stations(id_borne) ON DELETE SET NULL,
    CONSTRAINT fk_sig_res FOREIGN KEY (reservation_id) REFERENCES reservation(numero_reservation) ON DELETE SET NULL
);

-- ========================================
-- Table: utilisateur_lieu
-- ========================================
CREATE TABLE utilisateur_lieu (
    utilisateur_id BIGINT NOT NULL,
    lieu_id BIGINT NOT NULL,
    type_adresse VARCHAR(20) NOT NULL DEFAULT 'principale',
    PRIMARY KEY (utilisateur_id, lieu_id, type_adresse),

    CONSTRAINT fk_ul_user FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_ul_lieu FOREIGN KEY (lieu_id) REFERENCES lieu(id_lieu) ON DELETE CASCADE,
    CONSTRAINT chk_type_adresse CHECK (type_adresse IN ('principale','secondaire','travail'))
);

-- ========================================
-- Table: charging_station_lieu
-- ========================================
CREATE TABLE charging_station_lieu (
    borne_id BIGINT NOT NULL PRIMARY KEY,
    lieu_id BIGINT NOT NULL,

    CONSTRAINT fk_bl_borne FOREIGN KEY (borne_id) REFERENCES charging_stations(id_borne) ON DELETE CASCADE,
    CONSTRAINT fk_bl_lieu FOREIGN KEY (lieu_id) REFERENCES lieu(id_lieu) ON DELETE CASCADE
);

-- ========================================
-- Indexes
-- ========================================
CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_utilisateur_pseudo ON utilisateur(pseudo);
CREATE INDEX idx_charging_station_owner ON charging_stations(owner_id);
CREATE INDEX idx_charging_station_etat ON charging_stations(etat);
CREATE INDEX idx_charging_station_occupee ON charging_stations(occupee);
CREATE INDEX idx_reservation_user ON reservation(id_utilisateur);
CREATE INDEX idx_reservation_borne ON reservation(charging_station_id);
CREATE INDEX idx_reservation_dates ON reservation(date_debut, date_fin);
CREATE INDEX idx_avis_user ON avis(utilisateur_id);
CREATE INDEX idx_avis_borne ON avis(charging_station_id);
CREATE INDEX idx_signalement_user ON signalement(user_id);
CREATE INDEX idx_signalement_borne ON signalement(charging_station_id);
CREATE INDEX idx_signalement_statut ON signalement(statut);

-- ========================================
-- TRIGGER updated_at
-- ========================================
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_lieu_modtime BEFORE UPDATE ON lieu FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_utilisateur_modtime BEFORE UPDATE ON utilisateur FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_charging_modtime BEFORE UPDATE ON charging_stations FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_vehicule_modtime BEFORE UPDATE ON vehicule FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_reservation_modtime BEFORE UPDATE ON reservation FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_avis_modtime BEFORE UPDATE ON avis FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_signalement_modtime BEFORE UPDATE ON signalement FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ========================================
-- Trigger: Mise à jour auto état d'occupation
-- ========================================
CREATE OR REPLACE FUNCTION update_borne_occupation()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.etat = 'ACTIVE' AND (OLD.etat IS DISTINCT FROM 'ACTIVE') THEN
        UPDATE charging_stations SET occupee = TRUE WHERE id_borne = NEW.charging_station_id;
    END IF;

    IF NEW.etat IN ('TERMINEE','ANNULEE') AND OLD.etat = 'ACTIVE' THEN
        UPDATE charging_stations SET occupee = FALSE WHERE id_borne = NEW.charging_station_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- Vues finales
-- ========================================
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

CREATE OR REPLACE VIEW v_reservation_complete AS
SELECT
    r.numero_reservation,
    r.id_utilisateur,
    r.charging_station_id AS id_borne,
    r.date_debut,
    r.date_fin,
    r.prix_a_la_minute,
    r.etat,
    r.total_price,
    r.receipt_path,
    r.created_at,
    r.updated_at,
    u.nom AS utilisateur_nom,
    u.prenom AS utilisateur_prenom,
    u.email AS utilisateur_email,
    b.nom AS borne_nom,
    b.numero AS borne_numero,
    l.adresse AS borne_adresse,
    l.ville AS borne_ville
FROM reservation r
JOIN utilisateur u ON r.id_utilisateur = u.id_utilisateur
JOIN charging_stations b ON r.charging_station_id = b.id_borne
LEFT JOIN charging_station_lieu bl ON b.id_borne = bl.borne_id
LEFT JOIN lieu l ON bl.lieu_id = l.id_lieu;
