# What's in My Kitchen?

An intelligent kitchen assistant that tracks pantry inventory, matches recipes to ingredients you already have, and generates grocery lists for what you're missing.

The goal of the project is to build an AI-powered kitchen agent that can help answer questions like:

> "What can I make with what I already have?"

> "What do I need to buy to make Chicken Alfredo?"

> "What ingredients should I use before they expire?"

The application is currently being developed as a Spring Boot REST API backed by PostgreSQL, with an AI agent layer planned for future development.

## Features

### Pantry Management

Track ingredients currently available in the kitchen.

Each pantry item can contain:

- Ingredient name
- Quantity
- Unit
- Storage location
- Expiration date

Supported operations include:

- Add an ingredient
- View pantry inventory
- Update an ingredient
- Delete an ingredient

### Recipe Management

Store recipes and the ingredients required to make them.

Recipes currently include:

- Recipe name
- Number of servings
- Instructions
- Required ingredients
- Required quantity and unit for each ingredient

### Recipe Matching

Recipes can be compared against the current pantry inventory.

The application calculates a match percentage based on how much of each required ingredient is currently available.

Example:

```json
{
  "recipeId": 2,
  "recipeName": "Chicken Alfredo",
  "matchPercentage": 25.0,
  "ownedIngredients": [
    "Parmesan Cheese"
  ],
  "missingIngredients": [
    "Chicken Breast",
    "Fettuccine",
    "Heavy Cream"
  ]
}
```

### Grocery List Generation

The application can generate a grocery list for a specific recipe by comparing the recipe requirements with the pantry.

The response includes:

- Required quantity
- Available quantity
- Missing quantity
- Unit

Example:

```json
{
  "recipeId": 2,
  "recipeName": "Chicken Alfredo",
  "items": [
    {
      "name": "Chicken Breast",
      "requiredQuantity": 2.0,
      "availableQuantity": 0.0,
      "missingQuantity": 2.0,
      "unit": "pieces"
    },
    {
      "name": "Fettuccine",
      "requiredQuantity": 8.0,
      "availableQuantity": 0.0,
      "missingQuantity": 8.0,
      "unit": "oz"
    },
    {
      "name": "Heavy Cream",
      "requiredQuantity": 1.0,
      "availableQuantity": 0.0,
      "missingQuantity": 1.0,
      "unit": "cup"
    }
  ]
}
```

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate

### Database

- PostgreSQL

### Build

- Maven

## Project Structure

```text
src/main/java/com/marykatekitchen/mykitchen_agent/
├── controller/
│   ├── IngredientController.java
│   └── RecipeController.java
│
├── dto/
│   ├── GroceryItem.java
│   ├── GroceryListResponse.java
│   └── RecipeMatch.java
│
├── model/
│   ├── Ingredient.java
│   ├── Recipe.java
│   └── RecipeIngredient.java
│
├── repository/
│   ├── IngredientRepository.java
│   └── RecipeRepository.java
│
├── service/
│   ├── IngredientService.java
│   └── RecipeService.java
│
└── MykitchenAgentApplication.java
```

## API Endpoints

### Pantry

#### Get all pantry ingredients

```http
GET /api/ingredients
```

#### Add an ingredient

```http
POST /api/ingredients
```

Example request:

```json
{
  "name": "Parmesan Cheese",
  "quantity": 1,
  "unit": "cup",
  "location": "fridge",
  "expirationDate": "2026-09-01"
}
```

#### Update an ingredient

```http
PUT /api/ingredients/{id}
```

#### Delete an ingredient

```http
DELETE /api/ingredients/{id}
```

---

### Recipes

#### Get all recipes

```http
GET /api/recipes
```

#### Get a recipe

```http
GET /api/recipes/{id}
```

#### Add a recipe

```http
POST /api/recipes
```

Example request:

```json
{
  "name": "Chicken Alfredo",
  "servings": 2,
  "instructions": "Cook chicken. Boil pasta. Make sauce. Combine.",
  "ingredients": [
    {
      "name": "Chicken Breast",
      "quantity": 2,
      "unit": "pieces"
    },
    {
      "name": "Fettuccine",
      "quantity": 8,
      "unit": "oz"
    },
    {
      "name": "Parmesan Cheese",
      "quantity": 1,
      "unit": "cup"
    },
    {
      "name": "Heavy Cream",
      "quantity": 1,
      "unit": "cup"
    }
  ]
}
```

#### Update a recipe

```http
PUT /api/recipes/{id}
```

#### Delete a recipe

```http
DELETE /api/recipes/{id}
```

---

### Recipe Recommendations

#### Match recipes against the pantry

```http
GET /api/recipes/matches
```

Returns recipe match percentages along with owned and missing ingredients.

---

### Grocery Lists

#### Generate a grocery list for a recipe

```http
GET /api/recipes/{id}/grocery-list
```

The grocery list accounts for the quantity of an ingredient currently available when the pantry and recipe use matching units.

## Running Locally

### Prerequisites

Install:

- Java 21
- PostgreSQL

The project includes a Maven wrapper, so a separate Maven installation is not required.

### 1. Clone the repository

```bash
git clone YOUR_REPOSITORY_URL
cd mykitchen-agent
```

### 2. Create the PostgreSQL database

```bash
createdb kitchen_agent
```

### 3. Configure the database

Create:

```text
src/main/resources/application.properties
```

Add your local PostgreSQL configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kitchen_agent
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

`application.properties` is intentionally excluded from Git so local credentials and future application secrets are not committed to the repository.

### 4. Start the backend

```bash
./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## Current Limitations

The project is still under active development.

Current limitations include:

- Ingredient names must currently match between pantry items and recipe ingredients.
- Quantity comparisons currently require matching units.
- Unit conversions have not yet been implemented.
- Weight-to-volume conversions are intentionally not assumed because they can depend on the ingredient.
- Recipe data is currently added through the REST API rather than an external recipe provider.
- There is not yet a frontend interface or AI agent layer.

## Roadmap

Planned features include:

- Sort recipe recommendations by pantry match percentage
- Identify ingredients that are expiring soon
- Prioritize recipes that use ingredients before they expire
- Ingredient substitution recommendations
- Unit normalization and conversion
- Recipe search and discovery
- Persistent grocery lists
- Dietary preference and allergy support
- AI agent with tool calling
- Natural-language requests such as:
  - "What can I make tonight?"
  - "Give me something that takes less than 30 minutes."
  - "What should I make with the ingredients that are about to expire?"
- Frontend application for pantry, recipes, and grocery lists

## Long-Term Architecture

The long-term goal is for deterministic backend services to act as tools that an AI agent can use.

```text
User
  ↓
Frontend
  ↓
AI Kitchen Agent
  ↓
┌─────────────────────────────┐
│ Pantry Tool                 │
│ Recipe Matching Tool        │
│ Grocery List Tool           │
│ Substitution Tool           │
└─────────────────────────────┘
  ↓
Spring Boot Services
  ↓
PostgreSQL
```

Rather than relying on an LLM to determine pantry state or perform inventory calculations itself, the agent will use backend tools to retrieve and modify structured application data.

## Status

🚧 **In development**

The core pantry, recipe, recipe-matching, and grocery-list backend functionality is currently implemented. AI orchestration and the user interface are planned next.