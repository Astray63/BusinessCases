-- Données de test minimales
INSERT INTO utilisateur (id_utilisateur, nom, prenom, pseudo, email, mot_de_passe, role) 
VALUES (1, 'Admin', 'Test', 'admin', 'admin@test.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'admin');

INSERT INTO charging_stations (id_borne, nom, numero, localisation, latitude, longitude, puissance, etat, occupee, prix_a_la_minute, connector_type, description, address, hourly_rate, power_output, owner_id)
VALUES (1, 'Borne Test', 'TEST001', 'Localisation Test', 45.0, 5.0, 22, 'DISPONIBLE', false, 2.50, 'Type 2', 'Description test', '123 Test Street', 15.00, 22.0, 1);
