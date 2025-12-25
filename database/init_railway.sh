#!/bin/bash

# Configuration
DB_USER="postgres"
DB_NAME="railway"
# Password provided by user
export PGPASSWORD="bedCfb5aacfF114c6a3ecbd6A256aD2a"

echo "🚀 Helper script to initialize Railway Database"
echo "I have the User, Password, and DB Name defined."
echo "I just need the Public Domain and Port from you."
echo ""
echo "Find these in Railway -> Postgres Service -> Connect tab"
echo "Look for something like: viaduct.proxy.rlwy.net"
echo ""

read -p "Enter Host (Domain): " DB_HOST
read -p "Enter Port: " DB_PORT

if [ -z "$DB_HOST" ] || [ -z "$DB_PORT" ]; then
    echo "❌ Host and Port are required."
    exit 1
fi

echo ""
echo "⏳ Connecting to $DB_HOST:$DB_PORT..."

# 1. Run Schema
echo "📄 Running schema.sql..."
docker run --rm -i \
  -e PGPASSWORD=$PGPASSWORD \
  postgres:15-alpine \
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" < schema.sql

if [ $? -eq 0 ]; then
    echo "✅ Schema applied successfully."
else
    echo "❌ Failed to apply schema."
    exit 1
fi

# 2. Run Data
echo "📄 Running sample_data.sql..."
docker run --rm -i \
  -e PGPASSWORD=$PGPASSWORD \
  postgres:15-alpine \
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" < sample_data.sql

if [ $? -eq 0 ]; then
    echo "✅ Sample data imported successfully."
else
    echo "❌ Failed to import sample data."
    exit 1
fi

# 3. Verify Content
echo ""
echo "🔍 Verifying database content..."
docker run --rm -i \
  -e PGPASSWORD=$PGPASSWORD \
  postgres:15-alpine \
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
    SELECT 'Utilisateurs' as table_name, count(*) as count FROM utilisateur
    UNION ALL SELECT 'Lieux', count(*) FROM lieu
    UNION ALL SELECT 'Bornes', count(*) FROM borne
    UNION ALL SELECT 'Réservations', count(*) FROM reservation;
"

echo "🎉 Database initialized! You can now restart your Backend service on Railway."
