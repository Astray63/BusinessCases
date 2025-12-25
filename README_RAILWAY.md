# 🚀 Railway Deployment Guide

This project contains both a **Java Spring Boot Backend** and an **Angular Frontend**. To deploy on Railway, you must create **two separate services**, each pointing to its respective directory.

## 1. Prepare Repository
(Done) The root `package-lock.json` and `tsconfig.json` have been removed to avoid confusing Railway's auto-detection.

## 2. Deploy Backend (Spring Boot)
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
