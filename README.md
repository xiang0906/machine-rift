# Machine Rift MVP Backend

## Overview
This Spring Boot backend provides the initial MVP API layer for the Machine Rift tower defense project.

## Architecture
- Layered architecture: Controller -> Service -> Repository -> Entity
- RESTful JSON API
- Spring Data JPA + MySQL for production
- H2 for test execution
- DTO-based API contracts
- Centralized exception handling

## API Endpoints
- Players: `/api/players`
- Stages: `/api/stages`
- Towers: `/api/towers`
- Game records: `/api/game-records`
- Rankings: `/api/rankings`

## Swagger
OpenAPI UI is available at `/swagger-ui.html`.

## Database
Production uses MySQL. Update the connection settings in `src/main/resources/application.properties` before running locally.
