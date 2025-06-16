-- Connect to the database
\c electricity_business;

-- ========================================
-- Insert sample lieux (addresses)
-- ========================================
INSERT INTO lieu (adresse, nom, numero, rue, code_postal, ville, pays, region, latitude, longitude)
VALUES
('123 Rue de Rivoli, 75001 Paris', 'Centre de Paris', '123', 'Rue de Rivoli', '75001', 'Paris', 'France', 'Île-de-France', 48.8566, 2.3522),
('456 Rue de la République, 69002 Lyon', 'Centre de Lyon', '456', 'Rue de la République', '69002', 'Lyon', 'France', 'Auvergne-Rhône-Alpes', 45.7640, 4.8357),
('789 La Canebière, 13001 Marseille', 'Vieux Port Marseille', '789', 'La Canebière', '13001', 'Marseille', 'France', 'Provence-Alpes-Côte d\'Azur', 43.2965, 5.3698),
('321 Rue du Capitole, 31000 Toulouse', 'Capitole Toulouse', '321', 'Rue du Capitole', '31000', 'Toulouse', 'France', 'Occitanie', 43.6047, 1.4442),
('654 Promenade des Anglais, 06000 Nice', 'Promenade Nice', '654', 'Promenade des Anglais', '06000', 'Nice', 'France', 'Provence-Alpes-Côte d\'Azur', 43.7102, 7.2620),
('987 Avenue des Champs-Élysées, 75008 Paris', 'Champs-Élysées', '987', 'Avenue des Champs-Élysées', '75008', 'Paris', 'France', 'Île-de-France', 48.8600, 2.3500),
('741 Rue de la Part-Dieu, 69003 Lyon', 'Part-Dieu Lyon', '741', 'Rue de la Part-Dieu', '69003', 'Lyon', 'France', 'Auvergne-Rhône-Alpes', 45.7650, 4.8370),
('852 Boulevard Longchamp, 13001 Marseille', 'Longchamp Marseille', '852', 'Boulevard Longchamp', '13001', 'Marseille', 'France', 'Provence-Alpes-Côte d\'Azur', 43.2980, 5.3710),
('963 Place Wilson, 31000 Toulouse', 'Wilson Toulouse', '963', 'Place Wilson', '31000', 'Toulouse', 'France', 'Occitanie', 43.6060, 1.4460),
('159 Avenue Jean Médecin, 06000 Nice', 'Jean Médecin Nice', '159', 'Avenue Jean Médecin', '06000', 'Nice', 'France', 'Provence-Alpes-Côte d\'Azur', 43.7120, 7.2640);

-- Update geometry for lieux
UPDATE lieu SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- ========================================
-- Insert sample utilisateurs (users)
-- ========================================
INSERT INTO utilisateur (nom, prenom, pseudo, mot_de_passe, role, email, adresse_physique, date_naissance, age, medias, est_banni, email_verified)
VALUES
('Doe', 'John', 'johndoe123', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'john.doe@example.com', '123 Rue de Rivoli, 75001 Paris', '1985-03-15', 39, ARRAY['profile1.jpg', 'car1.jpg'], false, true),
('Smith', 'Jane', 'janesmith456', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'jane.smith@example.com', '456 Rue de la République, 69002 Lyon', '1990-07-22', 34, ARRAY['profile2.jpg'], false, true),
('Johnson', 'Alice', 'alicej789', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'alice.johnson@example.com', '789 La Canebière, 13001 Marseille', '1988-11-08', 36, ARRAY['profile3.jpg', 'car3.jpg'], false, true),
('Williams', 'Bob', 'bobwilliams', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'bob.williams@example.com', '321 Rue du Capitole, 31000 Toulouse', '1982-05-12', 42, ARRAY['profile4.jpg'], false, true),
('Brown', 'Charlie', 'charlieb', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'proprietaire', 'charlie.brown@example.com', '654 Promenade des Anglais, 06000 Nice', '1995-09-30', 29, ARRAY['profile5.jpg', 'car5.jpg'], false, true),
('Martin', 'Sophie', 'sophiem', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'sophie.martin@example.com', '12 Rue de la Paix, 75002 Paris', '1992-02-14', 32, ARRAY['profile6.jpg'], false, true),
('Dubois', 'Pierre', 'pierred', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'pierre.dubois@example.com', '34 Avenue de la Liberté, 69001 Lyon', '1987-12-03', 37, ARRAY['profile7.jpg'], false, true),
('Moreau', 'Marie', 'mariem', '$2a$10$3L7S.RV.GHLQv7lM3QFQveKpLlhDiRjQ5.gv.Z9TELXg4WGz9pnxu', 'client', 'marie.moreau@example.com', '78 Cours Julien, 13006 Marseille', '1993-06-18', 31, NULL, false, true);

-- ========================================
-- Insert liaisons utilisateur-lieu
-- ========================================
INSERT INTO utilisateur_lieu (utilisateur_id, lieu_id, type_adresse)
VALUES
(1, 1, 'principale'),
(2, 2, 'principale'),
(3, 3, 'principale'),
(4, 4, 'principale'),
(5, 5, 'principale');

-- ========================================
-- Insert sample bornes (charging stations)
-- ========================================
INSERT INTO borne (numero, nom, latitude, longitude, puissance, medias, instruction_sur_pied, etat, occupee, prix_a_la_minute, connector_type, description, owner_id)
VALUES
('B001', 'Borne Rivoli Paris', 48.8566, 2.3522, 7, ARRAY['borne1_1.jpg', 'borne1_2.jpg'], 'Insérer la carte, sélectionner la puissance, brancher le câble', 'disponible', false, 0.092, 'Type 2', 'Borne pratique en centre-ville de Paris', 1),
('B002', 'Station Rapide Lyon', 45.7640, 4.8357, 50, ARRAY['borne2_1.jpg'], 'Approcher votre badge, brancher le connecteur CCS', 'disponible', false, 0.146, 'CCS', 'Station de charge rapide avec parking couvert', 2),
('B003', 'Borne Eco Marseille', 43.2965, 5.3698, 25, ARRAY['borne3_1.jpg', 'borne3_2.jpg', 'borne3_3.jpg'], 'Utiliser l\'application mobile pour démarrer', 'disponible', false, 0.071, 'CHAdeMO', 'Borne écologique alimentée par panneaux solaires', 3),
('B004', 'Premium Toulouse', 43.6047, 1.4442, 22, ARRAY['borne4_1.jpg'], 'Code d\'accès: 1234, suivre les instructions à l\'écran', 'disponible', false, 0.167, 'Type 2', 'Station premium avec salon d\'attente', 4),
('B005', 'Budget Nice', 43.7102, 7.2620, 3, ARRAY['borne5_1.jpg'], 'Paiement par carte bancaire uniquement', 'disponible', false, 0.058, 'Type 1', 'Option de charge économique près de la plage', 5),
('B006', 'Centre Ville Paris', 48.8600, 2.3500, 50, ARRAY['borne6_1.jpg'], 'Accès 24h/7j, badge ou application mobile', 'disponible', false, 0.121, 'CCS', 'Emplacement central avec accès 24h/24', 1),
('B007', 'Centre Commercial Lyon', 45.7650, 4.8370, 11, ARRAY['borne7_1.jpg'], 'Chargez pendant vos achats, 4h maximum', 'disponible', false, 0.100, 'Type 2', 'Rechargez pendant que vous faites vos courses', 2),
('B008', 'Parc Bureau Marseille', 43.2980, 5.3710, 25, ARRAY['borne8_1.jpg'], 'Réservé employés 8h-18h, public le soir/weekend', 'disponible', false, 0.158, 'CHAdeMO', 'Idéal pour la recharge en entreprise', 3),
('B009', 'Résidentiel Toulouse', 43.6060, 1.4460, 7, ARRAY['borne9_1.jpg'], 'Zone calme, respecter le voisinage', 'disponible', false, 0.079, 'Type 2', 'Emplacement résidentiel calme', 4),
('B010', 'Hôtel Nice', 43.7120, 7.2640, 50, ARRAY['borne10_1.jpg'], 'Disponible pour clients hôtel et public', 'disponible', false, 0.133, 'CCS', 'Disponible pour les clients de l\'hôtel et le public', 5);

-- Update geometry for bornes
UPDATE borne SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326) WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- ========================================
-- Insert liaisons borne-lieu
-- ========================================
INSERT INTO borne_lieu (borne_id, lieu_id)
VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5),
(6, 6), (7, 7), (8, 8), (9, 9), (10, 10);

-- ========================================
-- Insert sample vehicules
-- ========================================
INSERT INTO vehicule (plaque_immatriculation, marque, modele, annee, capacite_batterie, user_id)
VALUES
('AB-123-CD', 'Tesla', 'Model 3', 2022, 75, 6),
('EF-456-GH', 'Renault', 'Zoe', 2021, 52, 7),
('IJ-789-KL', 'BMW', 'i3', 2020, 42, 8),
('MN-012-OP', 'Nissan', 'Leaf', 2023, 60, 1),
('QR-345-ST', 'Audi', 'e-tron', 2022, 95, 2);

-- ========================================
-- Insert sample reservations
-- ========================================
INSERT INTO reservation (id_utilisateur, id_borne, date_debut, date_fin, prix_a_la_minute, etat, total_price)
VALUES
(6, 1, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day' + INTERVAL '2 hours', 0.092, 'active', 11.04),
(7, 2, NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days' + INTERVAL '3 hours', 0.146, 'active', 26.28),
(8, 3, NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days' + INTERVAL '2 hours', 0.071, 'active', 8.52),
(1, 4, NOW() + INTERVAL '4 days', NOW() + INTERVAL '4 days' + INTERVAL '4 hours', 0.167, 'active', 40.08),
(2, 5, NOW() + INTERVAL '5 days', NOW() + INTERVAL '5 days' + INTERVAL '1 hour', 0.058, 'active', 3.48),
(6, 6, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '2 hours', 0.121, 'terminee', 14.52),
(7, 7, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '3 hours', 0.100, 'terminee', 18.00),
(8, 8, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '1 hour', 0.158, 'annulee', 9.48),
(1, 9, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 hours', 0.079, 'terminee', 9.48),
(2, 10, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '4 hours', 0.133, 'terminee', 31.92);

-- ========================================
-- Insert sample avis
-- ========================================
INSERT INTO avis (utilisateur_id, borne_id, note, commentaire)
VALUES
(6, 1, 4, 'Très pratique en centre-ville, facile d\'accès'),
(7, 2, 5, 'Charge rapide et parking couvert, parfait !'),
(8, 3, 4, 'Concept écologique intéressant, bon rapport qualité-prix'),
(1, 4, 5, 'Service premium excellent, salon d\'attente confortable'),
(2, 5, 3, 'Charge lente mais prix attractif'),
(6, 6, 4, 'Bien situé, accès 24h pratique'),
(7, 7, 4, 'Parfait pour faire les courses en même temps'),
(1, 9, 5, 'Quartier calme, idéal pour une charge tranquille'),
(2, 10, 4, 'Service hôtel de qualité, recommandé');

-- ========================================
-- Insert sample signalements
-- ========================================
INSERT INTO signalement (user_id, borne_id, reservation_id, description, statut, date_signalement)
VALUES
(8, 8, 8, 'Problème avec le connecteur CHAdeMO, ne se verrouille pas correctement', 'ouvert', NOW() - INTERVAL '3 days'),
(6, 1, NULL, 'Écran tactile peu réactif, difficile de naviguer dans les menus', 'en_cours', NOW() - INTERVAL '1 day'),
(7, 2, NULL, 'Place de parking trop étroite pour les gros véhicules', 'resolu', NOW() - INTERVAL '7 days'),
(1, 4, NULL, 'Salon d\'attente fermé pendant les heures annoncées', 'ouvert', NOW() - INTERVAL '2 days'),
(2, 5, NULL, 'Câble de charge endommagé, isolant visible', 'en_cours', NOW() - INTERVAL '6 hours');

-- ========================================
-- Mise à jour des statistiques
-- ========================================

-- Calculer l'âge automatiquement si la date de naissance est fournie
UPDATE utilisateur 
SET age = EXTRACT(YEAR FROM AGE(date_naissance)) 
WHERE date_naissance IS NOT NULL;

-- Mettre à jour l'état d'occupation des bornes basé sur les réservations actives
UPDATE borne SET occupee = true 
WHERE id_borne IN (
    SELECT DISTINCT id_borne 
    FROM reservation 
    WHERE etat = 'active' 
    AND date_debut <= NOW() 
    AND date_fin >= NOW()
);

-- ========================================
-- Commentaires et informations
-- ========================================

-- Afficher quelques statistiques
SELECT 'Utilisateurs créés: ' || COUNT(*) FROM utilisateur;
SELECT 'Lieux créés: ' || COUNT(*) FROM lieu;
SELECT 'Bornes créées: ' || COUNT(*) FROM borne;
SELECT 'Véhicules enregistrés: ' || COUNT(*) FROM vehicule;
SELECT 'Réservations créées: ' || COUNT(*) FROM reservation;
SELECT 'Avis donnés: ' || COUNT(*) FROM avis;
SELECT 'Signalements créés: ' || COUNT(*) FROM signalement;