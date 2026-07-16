# Machine Rift MVP Design

## System Overview
Machine Rift is a full-stack tower defense game. The MVP keeps gameplay in the frontend and uses the backend as a data service for players, stages, towers, game records, and rankings.

## Frontend Responsibilities
- Render the existing tower defense gameplay in Canvas
- Handle game state, enemies, towers, particles, and audio
- Use REST APIs for persistence instead of local storage over time

## Backend Responsibilities
- Expose RESTful APIs for players, stages, towers, game records, and ranking data
- Persist data in MySQL
- Keep business rules in the service layer
- Return consistent JSON responses

## Package Structure
- controller
- service
- repository
- entity
- dto
- config
- exception
- mapper
- util

## Core Database Tables
- player
- stage
- tower
- game_record

## API Summary
- GET /api/players
- GET /api/players/{id}
- POST /api/players
- PUT /api/players/{id}
- DELETE /api/players/{id}
- GET /api/stages
- GET /api/stages/{id}
- GET /api/towers
- GET /api/towers/{id}
- POST /api/game-records
- GET /api/game-records
- GET /api/game-records/{id}
- GET /api/rankings

## Design Principles
- Clean architecture and layered separation
- DTOs for API contracts
- Constructor injection via Lombok
- Validation on request payloads
- Global exception handling
- Maintainable and scalable MVP structure
