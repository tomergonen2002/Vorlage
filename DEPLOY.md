# DEPLOY.md — Deploy & Verifikation (Render + GitHub Actions)

Diese Datei beschreibt Schritt-für-Schritt, wie ihr das Projekt auf Render deployt, welche GitHub-Secrets gesetzt werden müssen und wie ihr M3/M4 verifiziert.

Voraussetzungen
- Ein Render-Account mit Rechten zum Erstellen von Services
- Ein GitHub-Repository (Branch `main`) mit diesem Projekt
- Optional: lokales `gradle` oder Gradle Wrapper, Node.js & npm

1) Backend-Service auf Render anlegen

- Option A — Ohne Docker (Render native build):
  1. In Render → New → Web Service → Connect repository → Branch `main`.
  2. Build Command: `./gradlew bootJar` (oder `./gradlew build`).
  3. Start Command: `java -jar build/libs/<your-jar-name>.jar` (Passe `<your-jar-name>` an, z. B. `rest-service-0.0.1-SNAPSHOT.jar`).
  4. Environment:
     - `JDBC_DATABASE_URL` = z. B. `postgres://user:pass@host:5432/dbname` (Render stellt DB oder extern)
     - `DB_USER` = postgres username
     - `DB_PASSWORD` = postgres password
  5. Optional: Set `SPRING_PROFILES_ACTIVE=prod` falls ihr prod-Profile nutzen wollt.

- Option B — Mit Docker:
  1. Erstelle eine `Dockerfile` im Repo, die das Spring Boot Jar erzeugt und startet.
  2. In Render Web Service → Environment: Docker.
  3. Render baut das Image per Dockerfile.

Hinweis: CORS ist bereits so konfiguriert, dass `https://*.onrender.com` erlaubt ist.

2) Frontend-Service auf Render anlegen

1. In Render → New → Static Site → Connect repository → Branch `main`.
2. Build Command: `cd frontend && npm ci && npm run build`
3. Publish Directory: `frontend/dist`
4. Environment:
   - `VITE_API_BASE` = `https://<your-backend-service>.onrender.com` (Backend-URL)

3) GitHub-Secrets & Optionaler GitHub Action Deploy

Wenn ihr die bereitgestellte GitHub Action `deploy-to-render.yml` benutzen wollt (manueller Dispatch), legt folgende Secrets im GitHub-Repo an:
- `RENDER_API_KEY` — API key aus Render (Account → API Keys → Create Key).
- `RENDER_FRONTEND_SERVICE_ID` — Service ID des Frontend in Render. (Render Dashboard → Frontend Service → Settings → Service ID in URL oder Settings.)
- `RENDER_BACKEND_SERVICE_ID` — Service ID des Backend in Render.

So setzt ihr die Secrets:
1. GitHub Repo → Settings → Secrets and variables → Actions → New repository secret
2. Name & Wert einfügen und speichern.

Manueller Deploy via Workflow (UI):
1. GitHub → Actions → Deploy to Render → Run workflow → wählen, welche Services deployet werden.

Oder per curl (lokal / CI):

```bash
curl -X POST https://api.render.com/v1/services/$SERVICE_ID/deploys \
  -H "Authorization: Bearer $RENDER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"clearCache":true}'
```

4) Lokales Testen (M3/M4 lokal prüfen)

- Backend starten (Default Port 8080):

```bash
# Wenn Gradle Wrapper vorhanden:
./gradlew bootRun

# Oder mit lokalem Gradle:
gradle bootRun
```

- Frontend starten (Vite, Default Port 5173):

```bash
cd frontend
export VITE_API_BASE=http://localhost:8080
npm ci
npm run dev
```

- Testen:
  - Öffne http://localhost:5173 → prüfe, ob die App lädt.
  - Erstelle eine Kategorie (UI) und eine Transaktion (UI). Dann prüfe via GET:

```bash
curl http://localhost:8080/transactions
curl http://localhost:8080/categories
```

Wenn die Einträge auftauchen, ist M4 lokal verifiziert.

## API Endpoints (kurze Referenz)

Die wichtigsten Endpunkte, die nach Deploy oder lokal verfügbar sind:

- `GET /` — Index (Willkommensnachricht)
- `GET /categories` — Kategorien (optional `userId`)
- `POST /categories` — Kategorie anlegen (CreateCategoryRequest JSON)
- `GET /transactions` — Alle Transaktionen (optional `userId`)
- `POST /transactions` — Transaktion anlegen (CreateTransactionRequest JSON)
- `GET /transactions/recent?limit=10&userId=...` — Letzte n Transaktionen
- `GET /stats?userId=...` — Aggregierte Kennzahlen (Total, Income, Expense, Net, byCategory)
- `POST /reset-dummy` — Dummy-Daten zurücksetzen (dev helper)

Nutze diese Endpunkte für die Verifikation nach Deploy; Beispiel-curl-Aufrufe findest du oben im Abschnitt "Lokales Testen".

5) Verifikation nach Deploy (Produktiv)

1. Öffne die Frontend-URL, die Render bereitstellt.
2. Erstelle Kategorie & Transaktion über UI.
3. Logge dich in Render (oder DB-Tool) ein und prüfe, ob in der Postgres-DB die neuen Reihen vorhanden sind.
4. Alternativ: Nutze curl mit der produktiven Backend-URL:

```bash
curl https://<your-backend>.onrender.com/transactions
```

6) Tipps & Troubleshooting

- Gradle Wrapper fehlt? Wenn `./gradlew` lokal nicht funktioniert, erzeugt man ihn mit lokal installiertem Gradle:

```bash
gradle wrapper
git add gradle/wrapper gradlew gradlew.bat
git commit -m "Add gradle wrapper"
git push
```

- CI: Backend-Workflow nutzt `gradle/gradle-build-action@v2` — wenn Wrapper vorhanden ist, läuft `./gradlew` ebenfalls.
- CORS: Backend erlaubt `https://*.onrender.com` bereits; falls Frontend auf einer anderen Domain liegt, passt `@CrossOrigin` in `FinanceController` an oder fügt die Domain in Render hinzu.

7) Checkliste bevor Demonstration / Abgabe

- [ ] Deploy Backend auf Render, env vars gesetzt
- [ ] Deploy Frontend auf Render, `VITE_API_BASE` gesetzt
- [ ] Test in Produktion: Kategorie & Transaktion anlegen → DB prüfen
- [ ] CI: Frontend & Backend Tests grün in GitHub Actions
- [ ] README: Produktiv-URLs und Verantwortliche eintragen

8) Weiteres (optional, kann ich für euch übernehmen)

- Ich kann die Gradle-Wrapper-Dateien ins Repo hinzufügen, damit `./gradlew` überall funktioniert (benötigt Bestätigung).
- Ich kann die `deploy-to-render.yml` so anpassen, dass es automatisch bei Push auf `main` deployed (benötigt GitHub Secrets).
- Ich kann einen kurzen Integrationstest mit Testcontainers (echtes Postgres) in CI ergänzen — das macht M4-verifikation robuster.

Wenn du möchtest, erstelle ich diese Optional-Änderungen jetzt (sag kurz: `ADD_WRAPPER`, `AUTO_DEPLOY` oder `TESTCONTAINERS`), ansonsten pushe deine Änderungen und trigger den Deploy wie oben.
