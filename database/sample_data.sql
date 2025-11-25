-- ========================================
-- Insert sample lieux (addresses)
-- ========================================
-- IMPORTANT: Assurez-vous d'être connecté à la base de données 'electricity' avant d'exécuter ce script
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
('Zeyko', 'User', 'Zeyko63', '$2b$12$gctjSVvz62dkbGEkVzdP4.KJXUyRXxCp9U9SJ3TCUhOmHpUrqP0cq', 'proprietaire', 'astray63000@gmail.com', '123 Rue de Rivoli, 75001 Paris', '1985-03-15', 39, false, true);


-- ========================================
-- Insert liaisons utilisateur-lieu
-- ========================================
INSERT INTO utilisateur_lieu (utilisateur_id, lieu_id, type_adresse)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 1, 'principale'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 2, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 3, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 4, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 5, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 6, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 7, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 8, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 9, 'secondaire'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), 10, 'secondaire');

-- ========================================
-- Insert sample bornes (charging stations)
-- ========================================
-- NOTE: Les owner_id correspondent aux propriétaires créés ci-dessus
-- On utilise les IDs des 5 premiers utilisateurs (qui sont tous propriétaires)
INSERT INTO charging_stations (numero, nom, localisation, address, latitude, longitude, puissance, instruction_sur_pied, etat, occupee, prix_a_la_minute, hourly_rate, connector_type, description, owner_id)
VALUES
('B001', 'Borne Rivoli Paris', '123 Rue de Rivoli, 75001 Paris', '123 Rue de Rivoli, 75001 Paris', 48.8566, 2.3522, 7, 'Insérer la carte, sélectionner la puissance, brancher le câble', 'DISPONIBLE', false, 0.092, 5.52, '2S', 'Borne pratique en centre-ville de Paris', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B002', 'Station Rapide Lyon', '456 Rue de la République, 69002 Lyon', '456 Rue de la République, 69002 Lyon', 45.7640, 4.8357, 50, 'Approcher votre badge, brancher le connecteur Type 2S', 'DISPONIBLE', false, 0.146, 8.76, '2S', 'Station de charge rapide avec parking couvert', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B003', 'Borne Eco Marseille', '789 La Canebière, 13001 Marseille', '789 La Canebière, 13001 Marseille', 43.2965, 5.3698, 25, 'Utiliser l''application mobile pour démarrer', 'DISPONIBLE', false, 0.071, 4.26, '2S', 'Borne écologique alimentée par panneaux solaires', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B004', 'Premium Toulouse', '321 Rue du Capitole, 31000 Toulouse', '321 Rue du Capitole, 31000 Toulouse', 43.6047, 1.4442, 22, 'Code d''accès: 1234, suivre les instructions à l''écran', 'DISPONIBLE', false, 0.167, 10.02, '2S', 'Station premium avec salon d''attente', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B005', 'Budget Nice', '654 Promenade des Anglais, 06000 Nice', '654 Promenade des Anglais, 06000 Nice', 43.7102, 7.2620, 3, 'Paiement par carte bancaire uniquement', 'DISPONIBLE', false, 0.058, 3.48, '2S', 'Option de charge économique près de la plage', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B006', 'Centre Ville Paris', '987 Avenue des Champs-Élysées, 75008 Paris', '987 Avenue des Champs-Élysées, 75008 Paris', 48.8600, 2.3500, 50, 'Accès 24h/7j, badge ou application mobile', 'DISPONIBLE', false, 0.121, 7.26, '2S', 'Emplacement central avec accès 24h/24', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B007', 'Centre Commercial Lyon', '741 Rue de la Part-Dieu, 69003 Lyon', '741 Rue de la Part-Dieu, 69003 Lyon', 45.7650, 4.8370, 11, 'Chargez pendant vos achats, 4h maximum', 'DISPONIBLE', false, 0.100, 6.00, '2S', 'Rechargez pendant que vous faites vos courses', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B008', 'Parc Bureau Marseille', '852 Boulevard Longchamp, 13001 Marseille', '852 Boulevard Longchamp, 13001 Marseille', 43.2980, 5.3710, 25, 'Réservé employés 8h-18h, public le soir/weekend', 'DISPONIBLE', false, 0.158, 9.48, '2S', 'Idéal pour la recharge en entreprise', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B009', 'Résidentiel Toulouse', '963 Place Wilson, 31000 Toulouse', '963 Place Wilson, 31000 Toulouse', 43.6060, 1.4460, 7, 'Zone calme, respecter le voisinage', 'DISPONIBLE', false, 0.079, 4.74, '2S', 'Emplacement résidentiel calme', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com')),
('B010', 'Hôtel Nice', '159 Avenue Jean Médecin, 06000 Nice', '159 Avenue Jean Médecin, 06000 Nice', 43.7120, 7.2640, 50, 'Disponible pour clients hôtel et public', 'DISPONIBLE', false, 0.133, 7.98, '2S', 'Disponible pour les clients de l''hôtel et le public', (SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'));

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
-- Insert sample reservations
-- ========================================
INSERT INTO reservation (id_utilisateur, charging_station_id, date_debut, date_fin, prix_a_la_minute, etat, total_price)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), NOW() + INTERVAL '4 days', NOW() + INTERVAL '4 days' + INTERVAL '4 hours', 0.167, 'ACTIVE', 40.08),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B009'), NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 hours', 0.079, 'TERMINEE', 9.48);

-- ========================================
-- Insert sample avis
-- ========================================
INSERT INTO avis (utilisateur_id, charging_station_id, note, commentaire)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), 5, 'Service premium excellent, salon d''attente confortable'),
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B009'), 5, 'Quartier calme, idéal pour une charge tranquille');

-- ========================================
-- Insert sample signalements
-- ========================================
INSERT INTO signalement (user_id, charging_station_id, reservation_id, description, statut, date_signalement)
VALUES
((SELECT id_utilisateur FROM utilisateur WHERE email = 'astray63000@gmail.com'), (SELECT id_borne FROM charging_stations WHERE numero = 'B004'), NULL, 'Salon d''attente fermé pendant les heures annoncées', 'OUVERT', NOW() - INTERVAL '2 days');

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
SELECT 'Réservations créées: ' || COUNT(*) FROM reservation;
SELECT 'Avis donnés: ' || COUNT(*) FROM avis;
SELECT 'Signalements créés: ' || COUNT(*) FROM signalement;