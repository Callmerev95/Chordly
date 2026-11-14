# T2 Handoff — Party Server Rebrand to Chordly

**Date:** 2026-11-14
**Completed by:** Claude (claude-haiku-5-5) via Callmerev95
**Branch:** `rebrand/chordly`
**Commits:** 5 (build fix, CORS fix, invite page rebrand, logo template, logo removed)

---

## What was delivered

1. **Invite page rebranded** — `backend/main.go` HTML template updated:
   - Page title: "Chordly Listen Together"
   - Copy: "Opening Chordly to sync playback in real time."
   - Button: "Join Party in Chordly"
   - Download link: `https://github.com/Callmerev95/Chordly-release/releases`
   - Logo removed (static file serving not working — see Open Issues below)

2. **GitHub Actions workflow fixed** — `.github/workflows/build-party-server.yml`:
   - Fixed: `go mod vendor` removed (vendored deps deleted), `go build` uses direct module path.
   - Fixed: artifact upload includes `chmod +x` in the upload step.
   - Artifact name: `chordly-party-server`.

3. **CORS preflight fixed** — `backend/main.go`:
   - `OPTIONS /ws/parties/*` returns 204 + `Access-Control-Allow-Origin: *`.
   - Android/web clients can now create parties without CORS errors.

4. **Binary deployed** — Server running at `https://chordly-party.thevibehosting.com/` with latest build.

5. **New issue filed** — #41: "[Bug] Listen Together: party restarts when YouTube track loops" (web-extension bug, not party server).

---

## Server Status

| Check | Result |
|-------|--------|
| Health endpoint | `{"ok":true}` |
| Status endpoint | `{"service":"chordly-listen-together"}` |
| Invite page title | "Chordly Listen Together" |
| Logo | Removed (404 — static file serving issue) |

**Running process:** `/root/chordly-party/chordly-party-server` on port 80.

---

## Files Changed

| File | Change |
|------|--------|
| `backend/main.go` | Invite page HTML rebrand + CORS fix |
| `.github/workflows/build-party-server.yml` | Build + artifact upload fix |

---

## Open Issues

### 1. Static file serving (LOW priority)
The logo `Chordly_logo.png` (740KB) exists at `/root/www/Chordly_logo.png` on the server, but returns 404 when accessed via `https://chordly-party.thevibehosting.com/Chordly_logo.png`.

**Root cause:** Likely the Caddyfile does not serve `/root/www/` or maps it differently. The invite page template references `/Chordly_logo.png`.

**Fix options:**
- Option A: Fix Caddy static file serving config to serve `/root/www/` at `/`.
- Option B: Host logo on GitHub Releases / CDN and reference external URL.
- Option C: Remove logo entirely (already done in latest commit) — page looks clean without it.

**Recommended:** Option C (no logo). If branding is needed, use text + CSS styling (already done).

### 2. Listen Together loop/restart bug (MEDIUM priority)
**Issue #41** — When YouTube track loops, ALL party members restart.

**Scope:** `web/extension/src/` — extension broadcast guard for `onPlaybackUpdate` events triggered by YouTube loop (not actual track changes).

**Not server-related** — Android app (bitchord scheme) does not have this issue.

---

## Next Steps for Follow-on Work

1. **T3 (Android scheme):** Change `bitchord://` intent scheme to `chordly://` in Android app and APK distribution. See [Issue #40](https://github.com/Callmerev95/Chordly/issues/40).

2. **T-loop (Issue #41):** Fix extension broadcast guard for YouTube loop events. Root cause already identified — `onPlaybackUpdate` fires with `positionMs ≈ 0` on every loop cycle without a guard to skip it.

3. **Logo/static files:** Confirm if logo is needed; if so, fix Caddy config or use external CDN.

---

## Verification Commands

```bash
# Check server health
curl https://chordly-party.thevibehosting.com/healthz

# Check invite page title
curl -s https://chordly-party.thevibehosting.com/invite/TESTCODE | grep -o '<title>.*</title>'

# Check status endpoint
curl -s https://chordly-party.thevibehosting.com/

# CORS test (create party)
curl -v -X OPTIONS https://chordly-party.thevibehosting.com/ws/parties/TEST \
  -H "Origin: https://example.com" \
  -H "Access-Control-Request-Method: GET"
# Should return: 204 No Content
```

---

## Handoff Confirmation

- [x] Invite page shows "Chordly Listen Together" ✅
- [x] CORS preflight returns 204 ✅
- [x] Server health OK ✅
- [x] Binary deployed ✅
- [x] Issue #41 filed for loop bug ✅
- [ ] T3 Android scheme — **pending** (issue #40 exists)
