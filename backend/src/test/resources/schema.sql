-- Création des tables minimales pour les tests
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    pseudo VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS charging_stations (
    id_borne BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    numero VARCHAR(50) NOT NULL,
    localisation VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    puissance INT,
    etat VARCHAR(20),
    occupee BOOLEAN,
    prix_a_la_minute DECIMAL(10,2),
    connector_type VARCHAR(50) DEFAULT '2S',
    description TEXT,
    address VARCHAR(255),
    hourly_rate DECIMAL(10,2),
    power_output DOUBLE,
    owner_id BIGINT,
    FOREIGN KEY (owner_id) REFERENCES utilisateur(id_utilisateur)
);
