# WebTech WiSe25/26 – Gruppenarbeit
Team Mitglieder: Tomer Gonen, Kolja Schollmeyer

Wir entwickeln eine Finanzplattform zur Verwaltung von Einnahmen und Ausgaben.

## Deployment (Render)

Kurz: das Projekt ist auf Render vorbereitbar. Die wichtigsten Schritte:

1. Erstelle zwei Services in Render (Backend & Frontend). Siehe `render.yaml` als Template.
2. Setze die Umgebungsvariablen für den Backend-Service:
	- `JDBC_DATABASE_URL`
	- `DB_USER`
	- `DB_PASSWORD`
3. Setze für den Frontend-Service `VITE_API_BASE` auf die URL des Backends (z. B. `https://finance-backend.onrender.com`).
4. CORS ist in der App für `https://*.onrender.com` freigegeben.

Nach dem Deployment bitte die aktiven URLs hier eintragen.

## Hinweise zu Milestones

- M1, M2: Implementiert (Backend-Startklasse, Entities, Vue-Komponenten mit `v-for`).
- M3: Vorbereitung abgeschlossen (CORS, prod properties, `render.yaml` existiert), Deployment noch durchzuführen in Render.
- M4: REST-POST-Endpunkte und Persistenz vorhanden; bitte nach Deploy testen, ob Schreiben in die produktive DB funktioniert.

---
Füge Deployment-URLs und CI-Status hinzu, sobald das Deployment auf Render erfolgt ist.

## Aktueller Projektstatus (Stand: 2025-11-02)

Untenstehende Tabelle zeigt, welche Milestones implementiert wurden, wo die Implementierung zu finden ist und welche nächsten Schritte nötig sind.

| Milestone | Status | Implementiert in / Evidence | Nächste Schritte |
|---|---:|---|---|
| M1 — Thema, Paar, Spring App mit GET-Route | ERLEDIGT | `README.md` (Team), `src/main/java/.../RestServiceApplication.java`, Entities unter `src/main/java/htw/webtech/financeMaster/persistence/entity` ; `FinanceController` enthält `@GetMapping("/")` | keine |
| M2 — Vue.js App + Unterkomponente mit `v-for` | ERLEDIGT | `frontend/src/components/CategoryList.vue` und `TransactionList.vue` verwenden `v-for`; `frontend/package.json`; Frontend-Tests unter `frontend/src/components/__tests__/` | keine |
| M3 — Frontend & Backend auf Render deployed; Frontend ruft GET | IN VORBEREITUNG (nicht deployed) | CORS erlaubt `https://*.onrender.com` (`FinanceController`), `src/main/resources/application-prod.properties` nutzt Render-typische env-vars; `render.yaml` (Template) und `.github/workflows/deploy-to-render.yml` (manuell) vorhanden | Deploy auf Render erstellen (Backend + Frontend), Env-Vars setzen (`JDBC_DATABASE_URL`, `DB_USER`, `DB_PASSWORD`, `VITE_API_BASE`), URLs in README eintragen |
| M4 — REST-API persistiert Daten, Frontend ruft POST | FUNKTIONAL IM CODE (noch nicht verifiziert in Prod) | `FinanceController` enthält `@PostMapping("/categories")` und `@PostMapping("/transactions")`; `TransactionRepository` extends `JpaRepository`; `frontend/src/components/TransactionForm.vue` sendet POST | Nach Deploy prüfen: Kategorie/Transaktion über UI anlegen und DB kontrollieren; ggf. Integrationstest in CI gegen reale DB ergänzen |

## Tests & CI

- Frontend CI: `.github/workflows/frontend-ci.yml` führt `npm ci`, `npm run test`, `npm run build` (erfolgreich lokal: Vitest-Tests sind grün).
- Backend CI: `.github/workflows/backend-ci.yml` wurde angepasst, um Gradle-Action zu nutzen; Integrationstest (`src/test/java/.../IntegrationTests.java`) ergänzt, nutzt in-memory H2 für test runs.
- Secrets/Credentials: Keine Klartext-Credentials im Repo gefunden; `application-prod.properties` verwendet Umgebungsvariablen.

## Lokales Testen / welche Localhost-URLs

Um das Projekt lokal zu testen, starte Backend (Spring Boot) und Frontend (Vite) getrennt. Empfohlenes Setup:

- Backend (Spring Boot): Standard-Port 8080
	- Startbefehl (falls Gradle Wrapper vorhanden):

```bash
./gradlew bootRun
```

	- Oder falls du lokal installiertes Gradle verwendest:

```bash
gradle bootRun
```

	- Backend ist dann unter: http://localhost:8080/

- Frontend (Vite): Standard-Port 5173
	- Wechsel in das Frontend-Verzeichnis und setze die API-Base-URL (damit FE Anfragen an das lokale Backend schickt):

```bash
cd frontend
export VITE_API_BASE=http://localhost:8080
npm ci
npm run dev
```

	- Frontend ist dann unter: http://localhost:5173/

Wichtig: Das Frontend hat standardmäßig `API_BASE = import.meta.env?.VITE_API_BASE || '/api'`. Daher ist es empfehlenswert, beim lokalen Test `VITE_API_BASE` auf `http://localhost:8080` zu setzen, damit FE-Requests direkt an den lokalen Backend-Server gehen. Alternativ kannst du auch einen Proxy konfigurieren.

## To‑Do / nächste Schritte (konkret)

1. Deploy auf Render durchführen (M3): Backend & Frontend Services anlegen, Umgebungsvariablen setzen. Trage die finalen URLs hier im README ein.
2. Nach Deploy: UI‑Test — Kategorie + Transaktion anlegen und prüfen, ob Daten in der Produktions-DB vorhanden sind (M4 verifizieren).
3. CI automatisieren (optional): Wenn gewünscht, aktiviere `deploy-to-render.yml` bei Push to `main` (benötigt GitHub-Secrets `RENDER_API_KEY`, `RENDER_FRONTEND_SERVICE_ID`, `RENDER_BACKEND_SERVICE_ID`).
4. Optional: Integrationstests in CI gegen Postgres (z. B. Testcontainers) ergänzen.
5. Optional: Falls du möchtest, ich kann die Gradle-Wrapper-Dateien ins Repo hinzufügen, damit `./gradlew` ohne lokale Gradle-Installation funktioniert — sag Bescheid.

---
Wenn du willst, erstelle ich noch eine detaillierte `DEPLOY.md` mit Schritt-für-Schritt Render-Anleitung und Screenshots (oder ich füge Gradle-Wrapper-Binaries hinzu). Sag kurz, welche Option du bevorzugst.