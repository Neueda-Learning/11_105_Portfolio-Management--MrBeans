# Finora — Product & Technical Requirements Document (PRD)

Version 1.1 · Single source of truth for how the codebase, repo, and infrastructure are structured. Anyone (or any AI coding agent) should be able to read this and know exactly where a given piece of logic lives, what to name it, and how it connects to everything else.

**Changelog from v1.0:** Price and FX data source resolved — both now backed by the `yahoofinance-api` (unofficial Yahoo Finance Java wrapper, no API key required), replacing the placeholder Alpha Vantage references. See Section 3.1, 3.3, 3.4, 6.4, 7, and 9.

---

## 0. Document Purpose

This PRD is the bridge between the Design Document (architecture/data model/business logic) and actual code. It answers: *"I'm about to write a file — which folder does it go in, what's it called, what does it depend on, and what does 'done' look like?"* Every section below maps 1:1 to a part of the repo.

---

## 1. Repository Layout (Monorepo)

```
portfolio-manager/
├── backend/                    # Spring Boot app — see Section 3
├── frontend/                   # React app — see Section 4
├── docker-compose.yml          # Section 6.1
├── Jenkinsfile                 # Section 6.3
├── .env.example                # Section 6.4
├── docs/
│   ├── design-document.md
│   ├── prd.md                  # this file
│   └── api-spec.yaml           # OpenAPI 3.0, kept in sync manually or via springdoc
├── .gitignore
└── README.md
```

**Rule:** nothing outside `backend/` or `frontend/` contains application code. `docs/` is documentation only. This keeps the two Dockerfiles able to `COPY` only their own subtree without pulling in unrelated files.

---

## 2. Naming & Coding Conventions (apply everywhere)

| Concern | Convention | Example |
|---|---|---|
| Java packages | lowercase, dot-separated, feature-based not layer-based at the top level | `com.portfoliomanager.investment` |
| Java classes | PascalCase, suffix indicates role | `InvestmentController`, `InvestmentService`, `InvestmentRepository` |
| DTOs | suffix `Request`/`Response` | `CreateInvestmentRequest`, `InvestmentResponse` |
| React components | PascalCase, one component per file, filename matches export | `DashboardCard.tsx` |
| React hooks | `useXxx.ts` | `usePortfolioSummary.ts` |
| TS types/interfaces | PascalCase, colocated in `types/` unless component-local | `Investment`, `TransactionType` |
| DB tables | snake_case, plural | `investments`, `price_snapshots` |
| DB columns | snake_case | `fx_rate_to_home` |
| REST paths | kebab-case, plural nouns, no verbs | `/investments/:id/transactions` |
| Git branches | `feature/<short-desc>`, `fix/<short-desc>` | `feature/dividend-simulation` |
| Commits | Conventional Commits | `feat(investment): add weighted-avg cost basis calc` |

---

## 3. Backend — `backend/`

### 3.1 Folder structure (feature-first, not layer-first at top level)

```
backend/
├── pom.xml
├── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/com/portfoliomanager/
│   │   │   ├── PortfolioManagerApplication.java      # @SpringBootApplication entry point
│   │   │   │
│   │   │   ├── investment/
│   │   │   │   ├── Investment.java                    # @Entity
│   │   │   │   ├── InvestmentRepository.java           # extends JpaRepository
│   │   │   │   ├── InvestmentService.java               # business logic
│   │   │   │   ├── InvestmentController.java             # @RestController
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateInvestmentRequest.java
│   │   │   │   │   ├── UpdateInvestmentRequest.java
│   │   │   │   │   └── InvestmentResponse.java
│   │   │   │   └── InvestmentType.java                  # enum: STOCK, BOND, CASH, OTHER
│   │   │   │
│   │   │   ├── transaction/
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionType.java                 # enum: BUY, SELL, DEPOSIT, WITHDRAWAL
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   └── dto/
│   │   │   │       ├── CreateTransactionRequest.java
│   │   │   │       └── TransactionResponse.java
│   │   │   │
│   │   │   ├── pricing/
│   │   │   │   ├── PriceSnapshot.java
│   │   │   │   ├── PriceSnapshotRepository.java
│   │   │   │   ├── PriceFeedClient.java                  # interface
│   │   │   │   ├── YahooFinancePriceFeedClient.java       # impl — sstrickx/yahoofinance-api wrapper
│   │   │   │   ├── FakePriceFeedClient.java               # test double, see Section 7
│   │   │   │   ├── PriceUpdateScheduler.java              # @Scheduled job, batches via get(String[])
│   │   │   │   └── PriceSnapshotService.java
│   │   │   │
│   │   │   ├── fxrate/
│   │   │   │   ├── FxRate.java
│   │   │   │   ├── FxRateRepository.java
│   │   │   │   ├── FxRateClient.java                     # interface, external FX API
│   │   │   │   ├── YahooFinanceFxRateClient.java          # impl — same library as pricing/
│   │   │   │   ├── FakeFxRateClient.java                  # test double, see Section 7
│   │   │   │   └── FxRateService.java                     # cache lookups + refresh
│   │   │   │
│   │   │   ├── dividend/
│   │   │   │   ├── Dividend.java
│   │   │   │   ├── DividendMode.java                     # enum: DISTRIBUTIVE, ACCUMULATIVE
│   │   │   │   ├── DividendRepository.java
│   │   │   │   ├── DividendService.java                  # applies 4.4 logic
│   │   │   │   ├── DividendController.java
│   │   │   │   └── dto/SimulateDividendRequest.java
│   │   │   │
│   │   │   ├── pnl/                                       # the "brain" — Section 4 math, no HTTP layer
│   │   │   │   ├── CostBasisCalculator.java               # 4.1 weighted-average
│   │   │   │   ├── PnlCalculator.java                     # 4.2 realised/unrealised
│   │   │   │   ├── CurrencyConverter.java                 # 4.3 dual FX rule
│   │   │   │   └── dto/PnlResult.java
│   │   │   │
│   │   │   ├── dashboard/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── DashboardService.java                  # composes pnl + pricing + fxrate
│   │   │   │   └── dto/
│   │   │   │       ├── DashboardSummaryResponse.java
│   │   │   │       ├── AllocationResponse.java
│   │   │   │       └── TrendResponse.java
│   │   │   │
│   │   │   ├── motivation/
│   │   │   │   ├── MotivationService.java                 # 4.5 threshold lookup
│   │   │   │   └── motivation-config.json                 # (in resources/, see 3.3)
│   │   │   │
│   │   │   ├── chatbot/
│   │   │   │   ├── ChatController.java                    # POST /chat
│   │   │   │   ├── ChatService.java                        # orchestrates Claude API call
│   │   │   │   ├── ClaudeApiClient.java                     # RestClient/WebClient wrapper
│   │   │   │   └── tools/
│   │   │   │       ├── PortfolioTool.java                   # interface all tools implement
│   │   │   │       ├── GetBestPerformerTool.java
│   │   │   │       ├── GetTodayFocusTool.java
│   │   │   │       └── ToolRegistry.java                    # maps tool name → implementation
│   │   │   │
│   │   │   ├── settings/
│   │   │   │   ├── UserSettings.java
│   │   │   │   ├── UserSettingsRepository.java
│   │   │   │   ├── UserSettingsService.java
│   │   │   │   └── UserSettingsController.java
│   │   │   │
│   │   │   ├── history/
│   │   │   │   ├── LedgerController.java                  # GET /history (read-only, cross-table)
│   │   │   │   └── LedgerService.java
│   │   │   │
│   │   │   └── common/
│   │   │       ├── config/
│   │   │       │   ├── WebClientConfig.java
│   │   │       │   ├── SchedulingConfig.java               # @EnableScheduling
│   │   │       │   └── CorsConfig.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java          # @RestControllerAdvice
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── ErrorResponse.java
│   │   │       └── util/
│   │   │           ├── MoneyMath.java                       # BigDecimal helpers, rounding rules
│   │   │           └── CalendarConverter.java                # LocalDate/Instant <-> java.util.Calendar
│   │   │                                                       # boundary adapter for yahoofinance-api calls
│   │   │
│   │   └── resources/
│   │       ├── application.yml                             # base config
│   │       ├── application-local.yml                        # profile: local docker-compose
│   │       ├── application-prod.yml                          # profile: prod
│   │       ├── motivation-config.json
│   │       └── db/migration/                                  # Flyway
│   │           ├── V1__init_schema.sql
│   │           ├── V2__seed_user_settings.sql
│   │           └── V3__add_metadata_json_column.sql
│   │
│   └── test/
│       └── java/com/portfoliomanager/
│           ├── pnl/
│           │   ├── CostBasisCalculatorTest.java             # highest priority — pure logic, no mocks
│           │   ├── PnlCalculatorTest.java
│           │   └── CurrencyConverterTest.java
│           ├── investment/InvestmentServiceTest.java
│           ├── dividend/DividendServiceTest.java
│           ├── pricing/PriceUpdateSchedulerTest.java          # uses FakePriceFeedClient, no live network
│           └── integration/
│               └── InvestmentControllerIntegrationTest.java  # @SpringBootTest + Testcontainers MySQL
```

### 3.2 Why "feature-first" packaging

Instead of `controllers/`, `services/`, `repositories/` as top-level folders (layer-first), each business concept (`investment`, `transaction`, `pnl`) owns its own folder containing all its layers. When you're implementing "dividend simulation," every file you touch lives in one folder — you're not jumping across four different top-level directories. `pnl/` deliberately has **no controller** — it's pure calculation logic, consumed by `dashboard` and `investment`, and is the easiest package to reach 100% unit test coverage on.

### 3.3 Key implementation details worth locking in now

- **`Investment.metadata`** maps to the JSON column via Hibernate's `@JdbcTypeCode(SqlTypes.JSON)` (Hibernate 6+) — store it as a `Map<String, Object>` or a small typed wrapper class, not a raw `String`.
- **All money fields are `BigDecimal`**, never `double`/`float`. `MoneyMath.java` centralizes rounding (e.g., `RoundingMode.HALF_UP`, scale 4 for FX rates, scale 2 for currency amounts) so every service rounds identically.
- **UUID primary keys**: `@Id @GeneratedValue(strategy = GenerationType.UUID)` mapped to `CHAR(36)`, per the design doc's MySQL note.
- **`PriceFeedClient` and `FxRateClient` are interfaces**, both now backed by the same **`yahoofinance-api`** library (`com.yahoofinance-api:YahooFinanceAPI`) — one dependency covers price snapshots *and* FX rates. Because the rest of the app only ever depends on these interfaces, swapping the provider later (if Yahoo's unofficial endpoints ever break) means writing one new class and flipping a config value — nothing else in `PriceUpdateScheduler`, `DashboardService`, or `pnl/` needs to change.
- **`CalendarConverter.java`** (new, in `common/util/`): the `yahoofinance-api` library predates `java.time` and expects/returns `java.util.Calendar`. This adapter converts `LocalDate`/`Instant` ↔ `Calendar` **right at the boundary** of `YahooFinancePriceFeedClient`/`YahooFinanceFxRateClient` — `Calendar` must never leak into any other package.
- **Batch calls preferred over per-symbol calls**: `PriceUpdateScheduler` should call `YahooFinance.get(String[] tickers)` (all held investments in one HTTP request) rather than looping and calling `YahooFinance.get(String)` per investment — same for FX pairs via `YahooFinance.getFx(String[])`. This is both faster and less likely to hit rate limits.
- **Graceful degradation on external API failures**: since `yahoofinance-api` wraps unofficial, undocumented Yahoo endpoints with no SLA, every call into it must be wrapped in try/catch. If one ticker fails, log a warning and skip that investment's snapshot for the cycle — never let one bad symbol crash the entire scheduled job.
- **Chatbot tools implement a shared `PortfolioTool` interface** (`String name()`, `Object execute(Map<String,Object> args)`), registered in `ToolRegistry` — adding a new chatbot capability later means adding one class + one registry entry, nothing else changes.
- **Flyway migrations are append-only** — never edit `V1__init_schema.sql` after it's been applied anywhere; new changes get a new `V2__...`, `V3__...` file. This is non-negotiable once the pipeline has run once.

### 3.4 application.yml — what config actually needs to exist

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/portfolio_manager
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate.ddl-auto: validate   # schema changes ONLY via Flyway, never auto-generated
  flyway:
    enabled: true
portfolio:
  home-currency-default: INR
  price-feed:
    provider: yahoo-finance        # no API key required
  claude:
    api-key: ${CLAUDE_API_KEY}
    model: claude-sonnet-4-6
```

`ddl-auto: validate` is deliberate — it makes Hibernate *refuse to start* if the entities don't match the Flyway-managed schema, catching drift immediately instead of silently auto-altering your production table.

### 3.5 `pom.xml` — new dependency

```xml
<dependency>
    <groupId>com.yahoofinance-api</groupId>
    <artifactId>YahooFinanceAPI</artifactId>
    <version>3.17.0</version> <!-- confirm latest available version before adding -->
</dependency>
```

---

## 4. Frontend — `frontend/`

### 4.1 Folder structure

```
frontend/
├── package.json
├── tsconfig.json
├── tailwind.config.ts
├── vite.config.ts                 # or CRA config, but Vite recommended for speed
├── Dockerfile
├── nginx.conf                      # for serving the build + proxying /api
├── public/
└── src/
    ├── main.tsx
    ├── App.tsx                      # router setup
    ├── api/
    │   ├── client.ts                 # fetch/axios wrapper, base URL, error handling
    │   ├── investments.ts             # typed API calls: getInvestments(), createInvestment()
    │   ├── transactions.ts
    │   ├── dashboard.ts
    │   ├── dividends.ts
    │   ├── settings.ts
    │   └── chat.ts
    ├── types/
    │   ├── investment.ts               # matches backend DTOs exactly
    │   ├── transaction.ts
    │   ├── dashboard.ts
    │   └── enums.ts                     # InvestmentType, TransactionType, DividendMode
    ├── store/
    │   ├── useSettingsStore.ts           # Zustand — home currency, update freq
    │   └── useFilterStore.ts             # shared filter state across list/history views
    ├── hooks/
    │   ├── usePortfolioSummary.ts
    │   ├── useInvestments.ts
    │   └── useChatbot.ts
    ├── pages/
    │   ├── Dashboard/
    │   │   ├── DashboardPage.tsx
    │   │   ├── SummaryCards.tsx
    │   │   ├── AllocationChart.tsx        # donut/treemap, Recharts
    │   │   ├── TrendChart.tsx              # line graph
    │   │   └── PerformanceScatter.tsx
    │   ├── Investments/
    │   │   ├── InvestmentListPage.tsx
    │   │   ├── InvestmentFilters.tsx
    │   │   └── InvestmentFormModal.tsx      # create/edit
    │   ├── InvestmentDetail/
    │   │   ├── InvestmentDetailPage.tsx
    │   │   ├── PnlBreakdown.tsx              # realised/unrealised, shown SEPARATELY
    │   │   └── TransactionHistoryTable.tsx
    │   ├── History/
    │   │   └── HistoryPage.tsx
    │   └── Settings/
    │       └── SettingsPage.tsx
    ├── components/
    │   ├── layout/
    │   │   ├── AppShell.tsx
    │   │   ├── Sidebar.tsx
    │   │   └── TopBar.tsx
    │   ├── ui/                              # generic, reusable, no business logic
    │   │   ├── Card.tsx
    │   │   ├── Table.tsx
    │   │   ├── Modal.tsx
    │   │   └── Badge.tsx
    │   └── ChatbotWidget/
    │       ├── ChatbotWidget.tsx             # floating, persistent across pages
    │       ├── ChatMessage.tsx
    │       └── ChatInput.tsx
    └── styles/
        └── index.css                          # Tailwind entrypoint
```

### 4.2 Key implementation rules

- **`types/` mirrors backend DTOs field-for-field.** If `InvestmentResponse.java` has `realisedPnl` and `unrealisedPnl` as separate fields, `types/investment.ts` has the exact same two fields — never a merged `totalPnl`. This is how the "never merge realised/unrealised" business rule stays enforced on the frontend too.
- **`api/` is the only place `fetch`/`axios` is called.** Pages and components never call the network directly — they call typed functions from `api/`, which return typed data. This makes swapping REST for anything else later a one-folder change.
- **No `<form>` tags** if any AI-generated Artifact code is reused here — but since this is a real deployed app (not a Claude Artifact), normal `<form onSubmit>` is fine and preferred for accessibility.
- **Zustand stores are small and specific** (`useSettingsStore`, `useFilterStore`) — resist the urge to create one giant global store; each store owns one concern.
- **Charts** (`AllocationChart`, `TrendChart`, `PerformanceScatter`) each take already-computed data as props — they contain zero calculation logic, only Recharts config. All math happened on the backend.

---

## 5. Database Migrations — `backend/src/main/resources/db/migration/`

| File | Purpose |
|---|---|
| `V1__init_schema.sql` | Creates all 7 tables from the design doc, with FKs, indexes on `investment_id` columns, and the JSON column for `metadata` |
| `V2__seed_user_settings.sql` | Inserts the single `user_settings` row with a sensible default (`home_currency = 'INR'`, `update_frequency = 'daily'`) so the app never queries an empty settings table |
| `V3+...` | Every future schema change — additive only where possible (new nullable column) to avoid breaking a running app mid-deploy |

**Indexing checklist for V1** (worth getting right immediately, not as an afterthought):
- `transactions(investment_id, txn_date)` — every P&L calc filters by investment then orders by date
- `price_snapshots(investment_id, fetched_at)` — trend graph and "latest price" both query this shape
- `fx_rates(from_currency, to_currency, rate_date)` — unique constraint here too, so the cron job can `UPSERT` instead of accumulating duplicate rates per day

---

## 6. DevOps

### 6.1 `docker-compose.yml` (local dev)

```yaml
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_DATABASE: portfolio_manager
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports: ["3306:3306"]
    volumes: ["mysql_data:/var/lib/mysql"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 10

  backend:
    build: ./backend
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      DB_HOST: mysql
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      CLAUDE_API_KEY: ${CLAUDE_API_KEY}
    ports: ["8080:8080"]

  frontend:
    build: ./frontend
    depends_on: [backend]
    ports: ["3000:80"]

volumes:
  mysql_data:
```

`depends_on: condition: service_healthy` matters — without it, the backend container can start and try to connect to MySQL before MySQL has finished initializing, causing flaky first-boot failures. Note there is no `PRICE_FEED_API_KEY` environment variable — the Yahoo Finance library requires none.

### 6.2 Dockerfiles

**`backend/Dockerfile`** (multi-stage):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**`frontend/Dockerfile`** (multi-stage):
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

`nginx.conf` needs a `location /api/ { proxy_pass http://backend:8080/; }` block so the React app can call `/api/investments` and nginx transparently forwards it to the backend container — this avoids CORS entirely in production since the browser only ever talks to one origin.

### 6.3 `Jenkinsfile` (declarative, matches Design Doc Section 8.2)

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') { steps { checkout scm } }

        stage('Backend Build & Test') {
            steps { dir('backend') { sh 'mvn verify' } }
        }

        stage('Frontend Build & Test') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                    sh 'npm test -- --watchAll=false'
                }
            }
        }

        stage('Build Images') {
            steps {
                sh "docker build -t portfolio-backend:${GIT_COMMIT} ./backend"
                sh "docker build -t portfolio-frontend:${GIT_COMMIT} ./frontend"
            }
        }

        stage('Push Images') {
            steps {
                sh "docker push portfolio-backend:${GIT_COMMIT}"
                sh "docker push portfolio-frontend:${GIT_COMMIT}"
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sh 'docker-compose pull && docker-compose up -d'
            }
        }
    }
}
```

`when { branch 'main' }` on Deploy is the gate mentioned in the design doc — feature branches run every stage except the last.

### 6.4 Environment Variables & Secrets

`.env.example` (checked into git, values blank — this is a template, never real secrets):
```
DB_USER=
DB_PASSWORD=
DB_ROOT_PASSWORD=
CLAUDE_API_KEY=
```

Note: no `PRICE_FEED_API_KEY` or `PRICE_FEED_PROVIDER` entries — the Yahoo Finance data source requires no API key, one less secret to manage in the Jenkins Credentials Store.

Real `.env` is git-ignored. In Jenkins, secrets are injected via Jenkins Credentials Store (not hardcoded in the Jenkinsfile) and passed as environment variables at build/deploy time.

---

## 7. Testing Strategy (what "done" means per layer)

| Layer | Tool | What's covered |
|---|---|---|
| `pnl/` package | JUnit 5, no Spring context | Every cost-basis, realised/unrealised, currency-conversion edge case — partial sells, multiple buys, FX rate changes over time. This package should have the highest coverage in the whole repo. |
| `pricing/` and `fxrate/` schedulers | JUnit 5 with `FakePriceFeedClient` / `FakeFxRateClient` | `PriceUpdateScheduler` and FX refresh logic tested against hardcoded fake responses — never against the live, unofficial Yahoo endpoints, so tests stay fast and don't depend on Yahoo being reachable or unrate-limited during CI runs |
| Services with DB access | `@DataJpaTest` + H2 or Testcontainers MySQL | Repository queries return correct rows |
| Controllers | `@SpringBootTest` + `MockMvc` or Testcontainers | Full request → response round trip, including validation errors |
| Frontend components | Vitest/Jest + React Testing Library | Rendering logic, especially that Realised/Unrealised never collapse into one number in the DOM |
| Frontend API layer | Mocked fetch | Correct request shape, error handling |

---

## 8. Definition of Done (per feature, applies to every PR)

1. Code lives in the correct feature package/folder per Section 3.1 / 4.1.
2. Money fields use `BigDecimal`, never float/double.
3. New DB changes are a new Flyway `V{n}__description.sql` file, never an edit to an existing one.
4. Unit tests exist for any new logic in `pnl/`.
5. New backend DTO fields are mirrored in the matching frontend `types/` file.
6. `mvn verify` and `npm run build && npm test` both pass locally before pushing.
7. No secrets committed — only `.env.example` placeholders.
8. Any code calling `yahoofinance-api` is wrapped in try/catch and degrades gracefully (logs + skips) rather than propagating a failure that could crash the scheduled job or a request thread.

---

## 9. Open Decisions Still To Resolve

1. ~~**Price data source**~~ — ✅ Resolved: `yahoofinance-api` (Yahoo Finance, no API key), covering both price snapshots and FX rates.
2. **Update frequency** — daily vs hourly. Since batch calls (`get(String[])`, `getFx(String[])`) fetch everything in one request, hourly is now cheap enough to consider from day one rather than deferring to Phase 3 — still a decision to make, not automatically resolved.
3. **Stock split handling** — the current data model has no concept of a split. `getSplitHistory()` is available in the library but nothing consumes it yet. Decide: ignore for MVP (documented known limitation) or add lightweight split-awareness later.
4. **Third-party API reliability fallback** — `yahoofinance-api` wraps unofficial, undocumented Yahoo endpoints with no SLA. Decide the failure-handling policy beyond "skip + log": e.g., alert if a ticker fails N days in a row, or fall back to the last known snapshot for display purposes.
5. **Weekly recap scope** — confirm with the customer whether this is in scope before building it.
6. **Documentation deliverable format** — README vs full user guide vs technical doc, so it can be scheduled into Phase 1 or 2.
7. **FIFO vs weighted-average** — confirmed recommendation is weighted-average, but worth a quick sanity check since it directly affects displayed realised P&L numbers.

---

**One-line summary:** every backend feature is a self-contained package (entity → repo → service → controller → dto), all money math lives in one dependency-free `pnl/` package that's unit-tested in isolation, price and FX data both come from a single no-key-required Yahoo Finance library sitting behind swappable interfaces, the frontend is a thin typed client over the REST API with zero business logic of its own, and the whole thing ships through one Jenkins pipeline that tests both halves before ever building a Docker image.