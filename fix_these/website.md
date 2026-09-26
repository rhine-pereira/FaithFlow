# Companion Website (`website/`)

Status: `tsc --noEmit` passes. `pnpm lint` fails (1 error). Run `pnpm audit` before deploying.

## 🟠 Deployment
- [ ] **Not zero-downtime** (`ecosystem.config.js:10`, `deployment/deploy.sh:95-104,150`): `pm2 reload` in fork mode with 1 instance is a plain restart, and `rsync --delete` swaps `.next/` under the running process. There's no health check or rollback. Deploy into `releases/<ts>/`, health-check, flip a `current` symlink, then reload, or use `exec_mode: 'cluster', instances: 2`.
- [ ] **Exposed port, no TLS** (`deploy.sh:172`, `ecosystem.config.js:8`): `next start -p 3008` binds 0.0.0.0. Add `-H 127.0.0.1` and put nginx/Caddy with HTTPS and HSTS in front.
- [ ] **No lockfile on the server** (`deploy.sh:133-136`): it runs `npm install` although the project uses pnpm. Use `corepack enable && pnpm install --prod --frozen-lockfile`, or `output: 'standalone'`.
- [ ] **rsync deletes the md5 marker** (`deploy.sh:95`): `--delete` removes `.package.json.md5` every time, so the dependency-skip never works. Add `--exclude='.package.json.md5'`.
- [ ] **`.env` files uploaded** (`deploy.sh:95`): only `.env*.local` is excluded, so `.env` / `.env.production` get uploaded or deleted. Add `--exclude='.env*'`.
- [ ] **False success** (`deploy.sh:159-166`): exits 0 and prints "Completed!" when pm2 is missing. Use `exit 1`.
- [ ] Quote the SSH flags as an array (`SSH_OPTS=(-i "$SSH_KEY")`), use `$HOME` instead of `~` inside quotes (line 116), and drop the `.bashrc` source under `set -u` (line 118).
- [ ] The server IP and user are hardcoded (`deploy.sh:19`). Move them to env or `~/.ssh/config`.
- [ ] Add `time: true` and log file settings (or pm2-logrotate) in `ecosystem.config.js`.

## 🟠 Security headers
- **Where:** `next.config.ts`
- [ ] Add `poweredByHeader: false`.
- [ ] Add a `headers()` block with CSP, `frame-ancestors 'none'` / X-Frame-Options, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `Permissions-Policy`, and HSTS (or set HSTS at the proxy).
- [ ] Remove the stale `allowedDevOrigins` (ngrok host and wildcards).

## 🟠 Branding / SEO
- [ ] `app/favicon.ico` is the default Next.js icon. Replace it with the FaithFlow icon and add `app/apple-icon.png`.
- [ ] `app/layout.tsx`: add `metadataBase`, `openGraph`, `twitter` and a canonical URL, plus `app/opengraph-image.png`, `app/robots.ts` and `app/sitemap.ts`.
- [ ] Delete the unused `public/next.svg`, `vercel.svg`, `file.svg`, `globe.svg`, `window.svg`, and rewrite the boilerplate `README.md`.
- [ ] Resize `public/faithflow-icon.png` (535 KB, shown at 28 px) to ≤512 px.

## 🟡 Accessibility
- [ ] The accent `#a8791f` on `#faf6ee` is ~3.6:1, which fails AA for small text and links (`globals.css:9,72`). Darken it to ~`#8a6216`.
- [ ] Add `aria-hidden="true"` to the decorative mock UIs in `app/page.tsx:61-222`.
- [ ] Add a Data Deletion link to `components/SiteFooter.tsx`.

## 🟡 Lint
- [ ] `ecosystem.config.js:1` `no-require-imports`: rename it to `.cjs` (and update `deploy.sh`), or add it to `globalIgnores`.

## ✅ Fine
- No secrets in website files.
- `set -euo pipefail` is used.
- Heading order, `lang="en"` and logo alt text are correct.
- Internal links resolve.
- Next 16.3.5 and React 19.2.8 are pinned.
