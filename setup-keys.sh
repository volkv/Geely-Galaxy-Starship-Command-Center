#!/bin/bash
set -e

KEYS_DIR="keys"
AOSP_BASE_URL="https://raw.githubusercontent.com/nickelulz/aosp-test-keys/main"
AOSP_FALLBACK_URL="https://raw.githubusercontent.com/nickelulz/aosp-test-keys/main"
WFAIRCLOUGH_URL="https://raw.githubusercontent.com/wfairclough/android_aosp_keys/master"

echo "=== GGSCC: Загрузка AOSP тестовых ключей ==="

mkdir -p "$KEYS_DIR"

download_file() {
    local file="$1"
    local url="$2"

    if curl -fsSL "$url" -o "$KEYS_DIR/$file" 2>/dev/null; then
        return 0
    fi
    return 1
}

download_with_fallback() {
    local file="$1"

    if [ -f "$KEYS_DIR/$file" ]; then
        echo "✓ $file уже существует"
        return 0
    fi

    echo "⬇ Скачиваю $file..."

    if download_file "$file" "$WFAIRCLOUGH_URL/$file"; then
        echo "✓ $file загружен"
        return 0
    fi

    echo "✗ Ошибка загрузки $file"
    return 1
}

download_with_fallback "platform.pk8"
download_with_fallback "platform.x509.pem"

if [ ! -f "$KEYS_DIR/platform.p12" ]; then
    echo "⚙ Создаю platform.p12 из pk8 + pem..."

    if ! command -v openssl &> /dev/null; then
        echo "✗ openssl не найден. Установите openssl и повторите."
        exit 1
    fi

    openssl pkcs8 -inform DER -nocrypt -in "$KEYS_DIR/platform.pk8" -out "$KEYS_DIR/platform.key"

    openssl pkcs12 -export \
        -in "$KEYS_DIR/platform.x509.pem" \
        -inkey "$KEYS_DIR/platform.key" \
        -out "$KEYS_DIR/platform.p12" \
        -name android \
        -passout pass:android

    rm -f "$KEYS_DIR/platform.key"
    echo "✓ platform.p12 создан"
else
    echo "✓ platform.p12 уже существует"
fi

echo ""
echo "=== Ключи готовы! Можно собирать проект: ==="
echo "  ./gradlew assemblePrivilegedRelease"
