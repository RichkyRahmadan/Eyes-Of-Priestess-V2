#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# generate-jwt-keys.sh — Generate RSA key pair for Sacred Seal (JWT) signing
# 
# Run once before starting the services:
#   chmod +x scripts/generate-jwt-keys.sh
#   ./scripts/generate-jwt-keys.sh
#
# The generated keys will be placed in each service's resources/META-INF/resources/
# ═══════════════════════════════════════════════════════════════════════════════

set -e

echo "🔮 Generating Sacred Seal RSA key pair for EyesOfPriestess..."

KEY_DIR="./backend/jwt-keys"
mkdir -p "$KEY_DIR"

# Generate RSA 2048-bit private key
openssl genrsa -out "$KEY_DIR/privateKey.pem" 2048
echo "✅ Private key generated: $KEY_DIR/privateKey.pem"

# Extract public key from private key
openssl rsa -in "$KEY_DIR/privateKey.pem" -pubout -out "$KEY_DIR/publicKey.pem"
echo "✅ Public key generated: $KEY_DIR/publicKey.pem"

# Copy to each service that needs JWT capabilities
SERVICES_WITH_SIGNING=(
    "seal-service"       # Signs tokens
)
SERVICES_WITH_VERIFY=(
    "the-veil"
    "vault-service"
    "covenant-service"
    "communion-service"
    "judgment-service"
)

# Copy private key (for signing) to seal-service only
for svc in "${SERVICES_WITH_SIGNING[@]}"; do
    DEST="./backend/$svc/src/main/resources/META-INF/resources"
    mkdir -p "$DEST"
    cp "$KEY_DIR/privateKey.pem" "$DEST/"
    cp "$KEY_DIR/publicKey.pem" "$DEST/"
    echo "✅ Keys copied to $svc"
done

# Copy public key only (for verification) to other services
for svc in "${SERVICES_WITH_VERIFY[@]}"; do
    DEST="./backend/$svc/src/main/resources/META-INF/resources"
    mkdir -p "$DEST"
    cp "$KEY_DIR/publicKey.pem" "$DEST/"
    echo "✅ Public key copied to $svc"
done

echo ""
echo "🏛️  Sacred Seal key generation complete!"
echo "⚠️  IMPORTANT: Never commit privateKey.pem to Git!"
echo "   Add 'backend/jwt-keys/' and '**/privateKey.pem' to .gitignore"
