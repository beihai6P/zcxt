#!/bin/bash

set -e

echo "=== Flyway Migration Repair Script ==="
echo "This script will repair the Flyway schema history table"
echo ""

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-zcxt}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-root}

echo "Database connection:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""

read -p "Do you want to continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "Operation cancelled."
    exit 1
fi

echo ""
echo "Step 1: Backing up flyway_schema_history table..."
mysqldump -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME flyway_schema_history > flyway_schema_history_backup_$(date +%Y%m%d_%H%M%S).sql 2>/dev/null || echo "Backup skipped (table may not exist or mysqldump not available)"

echo ""
echo "Step 2: Checking current migration status..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME -e "SELECT * FROM flyway_schema_history;" 2>/dev/null || echo "Table may not exist yet"

echo ""
echo "Step 3: Cleaning failed migrations and resetting schema history..."

mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME << 'EOF'
DELETE FROM flyway_schema_history WHERE success = 0;
DELETE FROM flyway_schema_history WHERE version IS NULL;
EOF

echo ""
echo "Step 4: Marking all applied migrations as succeeded..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME << 'EOF'
UPDATE flyway_schema_history SET success = 1 WHERE success = 0;
EOF

echo ""
echo "Step 5: Verifying schema history..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME -e "SELECT * FROM flyway_schema_history;"

echo ""
echo "=== Flyway repair completed ==="
echo "You can now restart your application to run migrations."