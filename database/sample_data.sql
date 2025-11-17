-- ========================================
-- Insert sample lieux (addresses)
-- ========================================
-- IMPORTANT: Assurez-vous d'être connecté à la base de données 'electricity_business' avant d'exécuter ce script
-- ========================================
INSERT INTO lieu (adresse, nom, numero, rue, code_postal, ville, pays, region, latitude, longitude)
VALUES
('123 Rue de Rivoli, 75001 Paris', 'Centre de Paris', '123', 'Rue de Rivoli', '75001', 'Paris', 'France', 'Île-de-France', 48.8566, 2.3522),
('456 Rue de la République, 69002 Lyon', 'Centre de Lyon', '456', 'Rue de la République', '69002', 'Lyon', 'France', 'Auvergne-Rhône-Alpes', 45.7640, 4.8357),
('789 La Canebière, 13001 Marseille', 'Vieux Port Marseille', '789', 'La Canebière', '13001', 'Marseille', 'France', 'Provence-Alpes-Côte d''Azur', 43.2965, 5.3698),
('321 Rue du Capitole, 31000 Toulouse', 'Capitole Toulouse', '321', 'Rue du Capitole', '31000', 'Toulouse', 'France', 'Occitanie', 43.6047, 1.4442),
('654 Promenade des Anglais, 06000 Nice', 'Promenade Nice', '654', 'Promenade des Anglais', '06000', 'Nice', 'France', 'Provence-Alpes-Côte d''Azur', 43.7102, 7.2620),
('987 Avenue des Champs-Élysées, 75008 Paris', 'Champs-Élysées', '987', 'Avenue des Champs-Élysées', '75008', 'Paris', 'France', 'Île-de-France', 48.8600, 2.3500),
('741 Rue de la Part-Dieu, 69003 Lyon', 'Part-Dieu Lyon', '741', 'Rue de la Part-Dieu', '69003', 'Lyon', 'France', 'Auvergne-Rhône-Alpes', 45.7650, 4.8370),
('852 Boulevard Longchamp, 13001 Marseille', 'Longchamp Marseille', '852', 'Boulevard Longchamp', '13001', 'Marseille', 'France', 'Provence-Alpes-Côte d''Azur', 43.2980, 5.3710),
('963 Place Wilson, 31000 Toulouse', 'Wilson Toulouse', '963', 'Place Wilson', '31000', 'Toulouse', 'France', 'Occitanie', 43.6060, 1.4460),
('159 Avenue Jean Médecin, 06000 Nice', 'Jean Médecin Nice', '159', 'Avenue Jean Médecin', '06000', 'Nice', 'France', 'Provence-Alpes-Côte d''Azur', 43.7120, 7.2640);

-- Update geometry for lieux
UPDATE lieu SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- ========================================
-- Insert sample utilisateurs (users)
-- ========================================
-- NOTE: Les 5 premiers utilisateurs sont des propriétaires de bornes
INSERT INTO utilisateur (nom, prenom, pseudo, mot_de_passe, role, email, adresse_physique, date_naissance, age, est_banni, email_verified)
VALUES
('Doe', 'John', 'johndoe123', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'john.doe@example.com', '123 Rue de Rivoli, 75001 Paris', '1985-03-15', 39, false, true),
('Smith', 'Jane', 'janesmith456', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'jane.smith@example.com', '456 Rue de la République, 69002 Lyon', '1990-07-22', 34, false, true),
('Johnson', 'Alice', 'alicej789', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'alice.johnson@example.com', '789 La Canebière, 13001 Marseille', '1988-11-08', 36, false, true),
('Williams', 'Bob', 'bobwilliams', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'bob.williams@example.com', '321 Rue du Capitole, 31000 Toulouse', '1982-05-12', 42, false, true),
('Brown', 'Charlie', 'charlieb', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'charlie.brown@example.com', '654 Promenade des Anglais, 06000 Nice', '1995-09-30', 29, false, true),
('Martin', 'Sophie', 'sophiem', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'sophie.martin@example.com', '12 Rue de la Paix, 75002 Paris', '1992-02-14', 32, false, true),
('Dubois', 'Pierre', 'pierred', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'pierre.dubois@example.com', '34 Avenue de la Liberté, 69001 Lyon', '1987-12-03', 37, false, true),
('Moreau', 'Marie', 'mariem', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'marie.moreau@example.com', '78 Cours Julien, 13006 Marseille', '1993-06-18', 31, false, true);

-- Insert medias pour utilisateurs
INSERT INTO utilisateur_medias (utilisateur_id, medias)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), 'profile1.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), 'car1.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), 'profile2.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'alice.johnson@example.com'), 'profile3.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'alice.johnson@example.com'), 'car3.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'bob.williams@example.com'), 'profile4.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'charlie.brown@example.com'), 'profile5.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'charlie.brown@example.com'), 'car5.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), 'profile6.jpg'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), 'profile7.jpg');

-- ========================================
-- Insert liaisons utilisateur-lieu
-- ========================================
INSERT INTO utilisateur_lieu (utilisateur_id, lieu_id, type_adresse)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), 1, 'principale'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), 2, 'principale'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'alice.johnson@example.com'), 3, 'principale'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'bob.williams@example.com'), 4, 'principale'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'charlie.brown@example.com'), 5, 'principale');

-- ========================================
-- Insert sample bornes (charging stations)
-- ========================================
-- NOTE: Les owner_id correspondent aux propriétaires créés ci-dessus
-- On utilise les IDs des 5 premiers utilisateurs (qui sont tous propriétaires)
INSERT INTO charging_stations (numero, nom, localisation, address, latitude, longitude, puissance, instruction_sur_pied, etat, occupee, prix_a_la_minute, hourly_rate, connector_type, description, owner_id)
VALUES
('B001', 'Borne Rivoli Paris', '123 Rue de Rivoli, 75001 Paris', '123 Rue de Rivoli, 75001 Paris', 48.8566, 2.3522, 7, 'Insérer la carte, sélectionner la puissance, brancher le câble', 'DISPONIBLE', false, 0.092, 5.52, 'Type 2', 'Borne pratique en centre-ville de Paris', (SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com')),
('B002', 'Station Rapide Lyon', '456 Rue de la République, 69002 Lyon', '456 Rue de la République, 69002 Lyon', 45.7640, 4.8357, 50, 'Approcher votre badge, brancher le connecteur CCS', 'DISPONIBLE', false, 0.146, 8.76, 'CCS', 'Station de charge rapide avec parking couvert', (SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com')),
('B003', 'Borne Eco Marseille', '789 La Canebière, 13001 Marseille', '789 La Canebière, 13001 Marseille', 43.2965, 5.3698, 25, 'Utiliser l''application mobile pour démarrer', 'DISPONIBLE', false, 0.071, 4.26, 'CHAdeMO', 'Borne écologique alimentée par panneaux solaires', (SELECT id_utilisateur FROM utilisateur WHERE email = 'alice.johnson@example.com')),
('B004', 'Premium Toulouse', '321 Rue du Capitole, 31000 Toulouse', '321 Rue du Capitole, 31000 Toulouse', 43.6047, 1.4442, 22, 'Code d''accès: 1234, suivre les instructions à l''écran', 'DISPONIBLE', false, 0.167, 10.02, 'Type 2', 'Station premium avec salon d''attente', (SELECT id_utilisateur FROM utilisateur WHERE email = 'bob.williams@example.com')),
('B005', 'Budget Nice', '654 Promenade des Anglais, 06000 Nice', '654 Promenade des Anglais, 06000 Nice', 43.7102, 7.2620, 3, 'Paiement par carte bancaire uniquement', 'DISPONIBLE', false, 0.058, 3.48, 'Type 1', 'Option de charge économique près de la plage', (SELECT id_utilisateur FROM utilisateur WHERE email = 'charlie.brown@example.com')),
('B006', 'Centre Ville Paris', '987 Avenue des Champs-Élysées, 75008 Paris', '987 Avenue des Champs-Élysées, 75008 Paris', 48.8600, 2.3500, 50, 'Accès 24h/7j, badge ou application mobile', 'DISPONIBLE', false, 0.121, 7.26, 'CCS', 'Emplacement central avec accès 24h/24', (SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com')),
('B007', 'Centre Commercial Lyon', '741 Rue de la Part-Dieu, 69003 Lyon', '741 Rue de la Part-Dieu, 69003 Lyon', 45.7650, 4.8370, 11, 'Chargez pendant vos achats, 4h maximum', 'DISPONIBLE', false, 0.100, 6.00, 'Type 2', 'Rechargez pendant que vous faites vos courses', (SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com')),
('B008', 'Parc Bureau Marseille', '852 Boulevard Longchamp, 13001 Marseille', '852 Boulevard Longchamp, 13001 Marseille', 43.2980, 5.3710, 25, 'Réservé employés 8h-18h, public le soir/weekend', 'DISPONIBLE', false, 0.158, 9.48, 'CHAdeMO', 'Idéal pour la recharge en entreprise', (SELECT id_utilisateur FROM utilisateur WHERE email = 'alice.johnson@example.com')),
('B009', 'Résidentiel Toulouse', '963 Place Wilson, 31000 Toulouse', '963 Place Wilson, 31000 Toulouse', 43.6060, 1.4460, 7, 'Zone calme, respecter le voisinage', 'DISPONIBLE', false, 0.079, 4.74, 'Type 2', 'Emplacement résidentiel calme', (SELECT id_utilisateur FROM utilisateur WHERE email = 'bob.williams@example.com')),
('B010', 'Hôtel Nice', '159 Avenue Jean Médecin, 06000 Nice', '159 Avenue Jean Médecin, 06000 Nice', 43.7120, 7.2640, 50, 'Disponible pour clients hôtel et public', 'DISPONIBLE', false, 0.133, 7.98, 'CCS', 'Disponible pour les clients de l''hôtel et le public', (SELECT id_utilisateur FROM utilisateur WHERE email = 'charlie.brown@example.com'));

-- Insert medias pour bornes
INSERT INTO borne_medias (borne_id, media_url)
VALUES
((SELECT id_borne FROM charging_stations WHERE numero = 'B001'), 'borne1_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B001'), 'borne1_2.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B002'), 'borne2_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B003'), 'borne3_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B003'), 'borne3_2.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B003'), 'borne3_3.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B004'), 'borne4_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B005'), 'borne5_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B006'), 'borne6_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B007'), 'borne7_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B008'), 'borne8_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B009'), 'borne9_1.jpg'),
((SELECT id_borne FROM charging_stations WHERE numero = 'B010'), 'borne10_1.jpg');

-- Update geometry for charging_stations
UPDATE charging_stations SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- ========================================
-- Insert liaisons borne-lieu
-- ========================================
INSERT INTO charging_station_lieu (borne_id, lieu_id)
VALUES
((SELECT id_borne FROM charging_stations WHERE numero = 'B001'), 1),
((SELECT id_borne FROM charging_stations WHERE numero = 'B002'), 2),
((SELECT id_borne FROM charging_stations WHERE numero = 'B003'), 3),
((SELECT id_borne FROM charging_stations WHERE numero = 'B004'), 4),
((SELECT id_borne FROM charging_stations WHERE numero = 'B005'), 5),
((SELECT id_borne FROM charging_stations WHERE numero = 'B006'), 6),
((SELECT id_borne FROM charging_stations WHERE numero = 'B007'), 7),
((SELECT id_borne FROM charging_stations WHERE numero = 'B008'), 8),
((SELECT id_borne FROM charging_stations WHERE numero = 'B009'), 9),
((SELECT id_borne FROM charging_stations WHERE numero = 'B010'), 10);

-- ========================================
-- Insert sample vehicules
-- ========================================
INSERT INTO vehicule (plaque_immatriculation, marque, modele, annee, capacite_batterie, user_id)
VALUES
('AB-123-CD', 'Tesla', 'Model 3', 2022, 75, (SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com')),
('EF-456-GH', 'Renault', 'Zoe', 2021, 52, (SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com')),
('IJ-789-KL', 'BMW', 'i3', 2020, 42, (SELECT id_utilisateur FROM utilisateur WHERE email = 'marie.moreau@example.com')),
('MN-012-OP', 'Nissan', 'Leaf', 2023, 60, (SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com')),
('QR-345-ST', 'Audi', 'e-tron', 2022, 95, (SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'));

-- ========================================
-- Insert sample reservations
-- ========================================
INSERT INTO reservation (id_utilisateur, charging_station_id, date_debut, date_fin, prix_a_la_minute, etat, total_price)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B001'), NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day' + INTERVAL '2 hours', 0.092, 'ACTIVE', 11.04),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B002'), NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days' + INTERVAL '3 hours', 0.146, 'ACTIVE', 26.28),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'marie.moreau@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B003'), NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days' + INTERVAL '2 hours', 0.071, 'ACTIVE', 8.52),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), NOW() + INTERVAL '4 days', NOW() + INTERVAL '4 days' + INTERVAL '4 hours', 0.167, 'ACTIVE', 40.08),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B005'), NOW() + INTERVAL '5 days', NOW() + INTERVAL '5 days' + INTERVAL '1 hour', 0.058, 'ACTIVE', 3.48),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B006'), NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '2 hours', 0.121, 'TERMINEE', 14.52),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B007'), NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '3 hours', 0.100, 'TERMINEE', 18.00),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'marie.moreau@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B008'), NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '1 hour', 0.158, 'ANNULEE', 9.48),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B009'), NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 hours', 0.079, 'TERMINEE', 9.48),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B010'), NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '4 hours', 0.133, 'TERMINEE', 31.92);

-- ========================================
-- Insert sample avis
-- ========================================
INSERT INTO avis (utilisateur_id, charging_station_id, note, commentaire)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B001'), 4, 'Très pratique en centre-ville, facile d''accès'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B002'), 5, 'Charge rapide et parking couvert, parfait !'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'marie.moreau@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B003'), 4, 'Concept écologique intéressant, bon rapport qualité-prix'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), 5, 'Service premium excellent, salon d''attente confortable'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B005'), 3, 'Charge lente mais prix attractif'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B006'), 4, 'Bien situé, accès 24h pratique'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B007'), 4, 'Parfait pour faire les courses en même temps'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B009'), 5, 'Quartier calme, idéal pour une charge tranquille'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B010'), 4, 'Service hôtel de qualité, recommandé');

-- ========================================
-- Insert sample signalements
-- ========================================
INSERT INTO signalement (user_id, charging_station_id, reservation_id, description, statut, date_signalement)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'marie.moreau@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B008'), (SELECT numero_reservation FROM reservation WHERE charging_station_id = (SELECT id_borne FROM charging_stations WHERE numero = 'B008') AND etat = 'ANNULEE'), 'Problème avec le connecteur CHAdeMO, ne se verrouille pas correctement', 'OUVERT', NOW() - INTERVAL '3 days'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'sophie.martin@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B001'), NULL, 'Écran tactile peu réactif, difficile de naviguer dans les menus', 'EN_COURS', NOW() - INTERVAL '1 day'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'pierre.dubois@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B002'), NULL, 'Place de parking trop étroite pour les gros véhicules', 'RESOLU', NOW() - INTERVAL '7 days'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'john.doe@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), NULL, 'Salon d''attente fermé pendant les heures annoncées', 'OUVERT', NOW() - INTERVAL '2 days'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'jane.smith@example.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B005'), NULL, 'Câble de charge endommagé, isolant visible', 'EN_COURS', NOW() - INTERVAL '6 hours');

-- ========================================
-- Mise à jour des statistiques
-- ========================================

-- Calculer l'âge automatiquement si la date de naissance est fournie
UPDATE utilisateur 
SET age = EXTRACT(YEAR FROM AGE(date_naissance)) 
WHERE date_naissance IS NOT NULL;

-- Mettre à jour l'état d'occupation des bornes basé sur les réservations actives
UPDATE charging_stations SET occupee = true 
WHERE id_borne IN (
    SELECT DISTINCT charging_station_id 
    FROM reservation 
    WHERE etat = 'ACTIVE' 
    AND date_debut <= NOW() 
    AND date_fin >= NOW()
);

-- ========================================
-- Commentaires et informations
-- ========================================

-- Afficher quelques statistiques
SELECT 'Utilisateurs créés: ' || COUNT(*) FROM utilisateur;
SELECT 'Lieux créés: ' || COUNT(*) FROM lieu;
SELECT 'Bornes créées: ' || COUNT(*) FROM charging_stations;
SELECT 'Véhicules enregistrés: ' || COUNT(*) FROM vehicule;
SELECT 'Réservations créées: ' || COUNT(*) FROM reservation;
SELECT 'Avis donnés: ' || COUNT(*) FROM avis;
SELECT 'Signalements créés: ' || COUNT(*) FROM signalement;