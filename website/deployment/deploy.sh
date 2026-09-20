#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Progressive Deployment Script for Faithflow Web (Next.js)
# - Incremental production build (Next.js cache)
# - Differential file sync via rsync (transfers ONLY changed files with live progress)
# - Preserves server node_modules (installs dependencies only if package.json changes)
# - Zero-downtime PM2 reload
# ==============================================================================

# Script & Project Root directory resolution
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

# Server & SSH Configuration (can be overridden via environment variables)
REMOTE_USER="${DEPLOY_USER:-rhine}"
REMOTE_HOST="${DEPLOY_HOST:-166.0.244.188}"
REMOTE_DIR="${DEPLOY_DIR:-/home/rhine/faithflow-web}"

# Auto-detect SSH identity key
if [ -n "${DEPLOY_KEY:-}" ]; then
    SSH_KEY="${DEPLOY_KEY}"
elif [ -f "${HOME}/.ssh/haphihost" ]; then
    SSH_KEY="${HOME}/.ssh/haphihost"
elif [ -f "${HOME}/.ssh/hapihhost" ]; then
    SSH_KEY="${HOME}/.ssh/hapihhost"
else
    SSH_KEY="${HOME}/.ssh/id_rsa"
fi

# Fix key permissions if the key file exists
if [ -f "${SSH_KEY}" ]; then
    chmod 600 "${SSH_KEY}" 2>/dev/null || true
fi

SSH_IDENTITY_FLAG=""
if [ -f "${SSH_KEY}" ]; then
    SSH_IDENTITY_FLAG="-i ${SSH_KEY}"
fi

echo "========================================================"
echo " Progressive Deployment: Faithflow Web"
echo " Target: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}"
echo " SSH Key: ${SSH_KEY}"
echo "========================================================"

# Check local dependencies
if ! command -v rsync >/dev/null 2>&1; then
    echo "Error: 'rsync' is required for progressive deployment." >&2
    echo "Please install it with: sudo apt-get install -y rsync" >&2
    exit 1
fi

# Step 1: Incremental Local Production Build
echo ""
echo "==> [Step 1/3] Running production build..."
if command -v node >/dev/null 2>&1 && command -v pnpm >/dev/null 2>&1; then
    pnpm run build
elif command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
    npm run build
elif command -v cmd.exe >/dev/null 2>&1; then
    echo "    (Using host toolchain via cmd.exe)"
    cmd.exe /c "pnpm run build" || cmd.exe /c "npm run build"
elif command -v pnpm >/dev/null 2>&1; then
    pnpm run build
elif command -v npm >/dev/null 2>&1; then
    npm run build
else
    echo "Error: Could not find Node.js package manager (pnpm or npm)." >&2
    exit 1
fi

# Ensure remote target directory exists and verify remote rsync
ssh ${SSH_IDENTITY_FLAG} "${REMOTE_USER}@${REMOTE_HOST}" "
  mkdir -p '${REMOTE_DIR}'
  if ! command -v rsync >/dev/null 2>&1; then
    echo 'Installing rsync on remote server...'
    sudo apt-get update -qq && sudo apt-get install -y -qq rsync
  fi
"

# Step 2: Progressive Differential Sync via Rsync
echo ""
echo "==> [Step 2/3] Progressively syncing changed files to server..."

# Rsync options:
# -a: archive mode (preserves permissions, times, symlinks)
# -v: verbose
# -z: compress file data during the transfer
# --delete: remove files remotely that were deleted locally (e.g. old .next build chunks)
# --info=progress2: display continuous overall transfer progress and speed
# --exclude: do NOT send git, cache, or local node_modules (server keeps its own native node_modules)
rsync -avz --info=progress2 --delete \
    -e "ssh ${SSH_IDENTITY_FLAG}" \
    --exclude='.git/' \
    --exclude='node_modules/' \
    --exclude='.next/cache/' \
    --exclude='.env*.local' \
    --exclude='deployment/' \
    --exclude='*.tar' \
    --exclude='*.tar.gz' \
    "${PROJECT_ROOT}/" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/"

# Step 3: Server dependencies check & PM2 zero-downtime reload
echo ""
echo "==> [Step 3/3] Finalizing on server & managing PM2..."
ssh ${SSH_IDENTITY_FLAG} "${REMOTE_USER}@${REMOTE_HOST}" bash -s -- "${REMOTE_DIR}" << 'EOF'
  set -euo pipefail

  TARGET_DIR="$1"
  cd "${TARGET_DIR}"

  # Ensure common binary paths for Node, npm, and PM2 are in PATH
  export PATH="$PATH:/usr/local/bin:/usr/bin:~/.nvm/versions/node/$(ls ~/.nvm/versions/node 2>/dev/null | tail -n 1)/bin:~/.npm-global/bin:~/.local/bin"
  [ -s "$HOME/.nvm/nvm.sh" ] && \. "$HOME/.nvm/nvm.sh" 2>/dev/null || true
  [ -s "$HOME/.bashrc" ] && source "$HOME/.bashrc" 2>/dev/null || true

  # Clean up broken Windows node_modules symlinks from previous transfers if present
  if [ -d "node_modules" ] && [ ! -f "node_modules/next/dist/bin/next" ]; then
      echo "==> Removing broken Windows node_modules symlinks..."
      rm -rf node_modules
  fi

  # Check if package.json has changed using an MD5 hash
  PKG_HASH=$(md5sum package.json | cut -d' ' -f1)
  LAST_HASH=""
  [ -f .package.json.md5 ] && LAST_HASH=$(cat .package.json.md5)

  if [ ! -f "node_modules/next/dist/bin/next" ] || [ "$PKG_HASH" != "$LAST_HASH" ]; then
      echo "==> Production dependencies need installing or updating. Running npm install..."
      if command -v npm >/dev/null 2>&1; then
          npm install --omit=dev --no-audit --no-fund
      elif command -v pnpm >/dev/null 2>&1; then
          pnpm install --prod
      else
          echo "Error: Neither npm nor pnpm found on server." >&2
          exit 1
      fi
      echo "$PKG_HASH" > .package.json.md5
  else
      echo "==> Production dependencies are up-to-date. Skipping npm install."
  fi

  echo "==> Managing PM2 process..."
  if command -v pm2 >/dev/null 2>&1; then
      if pm2 describe faithflow-web >/dev/null 2>&1; then
          echo "    Reloading 'faithflow-web' (zero-downtime hot reload)..."
          pm2 reload ecosystem.config.js --env production
      else
          echo "    Starting 'faithflow-web'..."
          pm2 start ecosystem.config.js --env production
      fi
      pm2 save
      echo ""
      echo "==> PM2 Status:"
      pm2 status faithflow-web
  else
      echo ""
      echo "==> NOTICE: 'pm2' was not detected in PATH on the server."
      echo "    To start your app with PM2, run:"
      echo "      sudo npm install -g pm2"
      echo "      cd ${TARGET_DIR}"
      echo "      pm2 start ecosystem.config.js --env production"
  fi
EOF

echo ""
echo "========================================================"
echo " Progressive Deployment Completed!"
echo " App running on http://${REMOTE_HOST}:3008"
echo "========================================================"
