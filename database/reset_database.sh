#!/bin/bash

# Script pour recréer la base de données electricity_business

DB_NAME="electricity_business"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo "🔄 Recréation de la base de données $DB_NAME..."

# Drop et recréer la base
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "DROP DATABASE IF EXISTS $DB_NAME;"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE $DB_NAME;"

echo "✅ Base de données recréée"

# Exécuter le schéma
echo "📋 Création du schéma..."
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f schema.sql

echo "✅ Schéma créé"

# Insérer les données d'exemple (optionnel)
read -p "Voulez-vous insérer les données d'exemple ? (o/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Oo]$ ]]
then
    echo "📊 Insertion des données d'exemple..."
    psql -U $DB_USER -h $DB_HOST -p $DB_PORT -d $DB_NAME -f sample_data.sql
    echo "✅ Données insérées"
fi

echo "🎉 Terminé !"
