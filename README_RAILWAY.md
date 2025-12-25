# 🚀 Railway Deployment Guide

This project contains both a **Java Spring Boot Backend** and an **Angular Frontend**. To deploy on Railway, you must create **two separate services**, each pointing to its respective directory.

## 1. Prepare Repository
(Done) The root `package-lock.json` and `tsconfig.json` have been removed to avoid confusing Railway's auto-detection.

## 2. Deploy Database (PostgreSQL)
1.  **New Service** -> **Database** -> **Add PostgreSQL**.
2.  Wait for the service to initialize.
3.  **Run Initialization Scripts**:
    *   Since this is a fresh database, you need to load your schema and data.
    *   You can do this via the Railway CLI or by connecting a local tool (like pgAdmin or DBeaver) using the credentials provided in the "Connect" tab of the Postgres service.
    *   **Files to run (in order)**:
        1.  `database/schema.sql`
        2.  `database/sample_data.sql`
    *   *Note: If your application has `spring.jpa.hibernate.ddl-auto=update` (check your variables), it might create tables automatically, but running `schema.sql` ensures specific types (like PostGIS geometry) are set up correctly.*

## 3. Deploy Backend (Spring Boot)
1.  **New Service** -> **GitHub Repo** -> Select this repository.
2.  Go to **Settings** -> **General** -> **Root Directory**.
3.  Set Root Directory to: `/backend`
4.  Go to **Variables**.
5.  Add the necessary environment variables (refer to `.env.example` or your docker-compose), typically:
    *   `SPRING_DATASOURCE_URL`
    *   `SPRING_DATASOURCE_USERNAME`
    *   `SPRING_DATASOURCE_PASSWORD`
    *   (If using a Railway Postgres Plugin, use `${DATABASE_URL}`)
    *   **Note**: For `SPRING_DATASOURCE_URL`, if using Railway Postgres, it usually looks like `jdbc:postgresql://<host>:<port>/<db>`.

#### Required Environment Variables (Backend)
Based on your configuration, you **MUST** add these in the **Variables** tab of your Backend Service:

| Variable | Value (Example) |
| :--- | :--- |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://postgis.railway.internal:5432/railway` | **Crucial:** Use the **Private URL** (internal) for better performance. It usually looks like `jdbc:postgresql://postgis.railway.internal:5432/railway`. Check the "Connect" -> "Private Net" tab in your Postgres service. |
| `SPRING_DATASOURCE_USERNAME` | `postgres`                                    | Default user. |
| `SPRING_DATASOURCE_PASSWORD` | (Check Variables)                             | The password from your Postgres service variables (`PGPASSWORD`). |
| `APP_JWT_SECRET` | `your-very-long-secret-key-at-least-32-chars` |
| `BREVO_API_KEY` | `xkeysib-...` |
| `BREVO_SENDER_EMAIL` | `contact@yourdomain.com` |
| `BREVO_SENDER_NAME` | `Electricity Business` |

> **Pro Tip:** In Railway, if you add a PostgreSQL database service, it provides a `DATABASE_URL` variable. You can often use `${DATABASE_URL}` but Spring Boot needs the `jdbc:` prefix.
> So set `SPRING_DATASOURCE_URL` to `jdbc:${DATABASE_URL}` (Railway might require some tweaking if protocols mismatch, but usually `jdbc:postgresql://...` is safe).
6.  Railway should automatically detect the `pom.xml` and `Dockerfile` (if present) or use the Java buildpack.

## 3. Deploy Frontend (Angular)
1.  **New Service** -> **GitHub Repo** -> Select this repository (again).
2.  Go to **Settings** -> **General** -> **Root Directory**.
3.  Set Root Directory to: `/frontend`
4.  Railway should detect `package.json` and use Node.js to build.
5.  **Important**: Ensure your `package.json` in `frontend/` has a `build` script and a `start` script (or configure the Start Command in Railway Settings).
    *   Typical Start Command for Angular on Nginx (if using Dockerfile): Dockerfile handles it.
    *   If using Node buildpack: `npm run start` (but usually Angular needs to be served statically or via a small Node server).
    *   **Recommended**: Rely on the `frontend/Dockerfile` if it exists.

## 4. Connect Frontend to Backend
1.  Get the **Public Domain** of your Backend service from Railway.
2.  In your Frontend service variables, add:
    *   `API_URL` (or your specific variable): `https://<your-backend-url>`
