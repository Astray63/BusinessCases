-- Données de test minimales
DELETE FROM charging_stations;
DELETE FROM utilisateur;

INSERT INTO utilisateur (nom, prenom, pseudo, email, mot_de_passe, role) 
SELECT 'Admin', 'Test', 'admin', 'admin@test.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE pseudo = 'admin');

INSERT INTO charging_stations (nom, numero, localisation, latitude, longitude, puissance, etat, occupee, prix_a_la_minute, connector_type, description, address, hourly_rate, power_output)
SELECT 'Borne Test', 'TEST001', 'Localisation Test', 45.0, 5.0, 22, 'DISPONIBLE', false, 2.50, 'Type 2', 'Description test', '123 Test Street', 15.00, 22.0
WHERE NOT EXISTS (SELECT 1 FROM charging_stations WHERE numero = 'TEST001');
