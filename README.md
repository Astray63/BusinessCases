# Electricity Business (EB)

A web application that connects electric vehicle owners with charging station owners, enabling the rental and booking of charging stations.

## Table of Contents
- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Development Guidelines](#development-guidelines)
- [Testing](#testing)
- [License](#license)
- [🐳 Lancer l'application avec Docker](#🐳-lancer-l'application-avec-docker)

## Project Overview

Electricity Business (EB) is a platform designed to connect electric vehicle owners with individuals who own charging stations. The application enables users to:

- Register and manage their own charging stations
- Set hourly rates for their charging stations
- Find nearby available charging stations on a map
- Book charging stations for specific time slots
- Manage reservations (accept/reject/cancel)
- Generate PDF receipts for completed reservations
- Export reservation history to Excel

## Project Structure

The project is organized into a modern multi-module architecture:

```
electricity-business/
│
├── frontend/               # Angular-based web client
│   ├── src/                # Application source code
│   │   ├── app/            # Angular components, services, etc.
│   │   │   ├── components/ # Reusable UI components
│   │   │   ├── models/     # TypeScript interfaces and models
│   │   │   ├── pages/      # Page components
│   │   │   ├── services/   # API and business logic services
│   │   │   └── shared/     # Shared utilities and helpers
│   │   ├── assets/         # Static assets
│   │   └── environments/   # Environment configurations
│   ├── angular.json        # Angular configuration
│   └── package.json        # Frontend dependencies
│
├── backend/                # Spring Boot backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/electriccharge/app/
│   │   │   │   ├── config/       # Application configuration
│   │   │   │   ├── controller/   # REST API controllers
│   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   ├── exception/    # Custom exceptions
│   │   │   │   ├── model/        # JPA entity models
│   │   │   │   ├── repository/   # Database repositories
│   │   │   │   ├── service/      # Business logic services
│   │   │   │   └── util/         # Utility classes
│   │   │   └── resources/        # Application properties and resources
│   │   └── test/                 # Test classes
│   └── pom.xml                   # Backend dependencies
│
└── database/              # Database scripts
    ├── schema.sql         # Database schema definition
    └── sample_data.sql    # Sample data for development
```

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT authentication
- Spring Data JPA
- Hibernate ORM
- PostgreSQL database
- Maven build system
- iText PDF for receipt generation
- Apache POI for Excel export

### Frontend
- Angular
- Bootstrap
- TypeScript
- Leaflet for maps integration

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js and npm
- PostgreSQL database
- Maven

### Backend Setup
1. Clone the repository
2. Configure the database connection in `backend/src/main/resources/application.properties`
3. Navigate to the backend directory: `cd backend`
4. Build the project: `mvn clean install`
5. Run the application: `mvn spring-boot:run`

The backend API will be available at `http://localhost:8080/api`

### Frontend Setup
1. Navigate to the frontend directory: `cd frontend`
2. Install dependencies: `npm install`
3. Run the development server: `ng serve`

The frontend application will be available at `http://localhost:4200`

### Database Setup
1. Install PostgreSQL
2. Create a database for the application
3. Run the scripts in the database directory:
   ```
   psql -U postgres -f database/schema.sql
   psql -U postgres -f database/sample_data.sql
   ```

## API Documentation

The API documentation is available at `http://localhost:8080/api/swagger-ui/` when the backend is running.

## Development Guidelines

### Backend Development
- Follow SOLID principles
- Use DTOs for data transfer between layers
- Implement proper exception handling
- Write unit and integration tests
- Document all public APIs

### Frontend Development
- Follow Angular best practices
- Use Angular reactive forms
- Implement responsive UI design
- Separate concerns (services, components, models)
- Use lazy loading for feature modules

### Git Workflow
- Use feature branches
- Write meaningful commit messages
- Create pull requests for code review
- Merge into main branch after approval

## Testing

Run backend tests with:
```
cd backend
mvn test
```

Run frontend tests with:
```
cd frontend
ng test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors

- Your Name - Initial work 

## 🐳 Lancer l'application avec Docker

Assurez-vous d'avoir Docker et Docker Compose installés.

```bash
# À la racine du projet
docker compose up --build -d
```

Services exposés :

| Service   | Port hôte | Description                        |
|-----------|-----------|------------------------------------|
| PostgreSQL| 5432      | Base de données                    |
| Backend   | 8080      | API Spring Boot (`/api`…)          |
| Frontend  | 4200      | Application Angular (prod)         |

Arrêt et suppression :

```bash
docker compose down
``` 