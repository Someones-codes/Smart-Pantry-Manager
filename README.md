# Smart Pantry Manager

A native Android app (Java) that helps users reduce food waste. The user records
the ingredients they already have, and the app suggests only recipes they can cook
right now with those ingredients, with no shopping trip required.

Module: Mobile App Development 700, Richfield Graduate Institute

## The strict-matching rule
A recipe is suggested only if EVERY ingredient it needs is in the pantry in at least
the required quantity. If even one ingredient is missing or short, the recipe is hidden.
Matching handles plurals and capitalisation ("Tomatoes" = "tomato") and compatible
units (1 kg covers 250 g; tbsp/tsp convert to ml). Units from different families
(for example pcs vs ml) are never compared. Logic: `RecipeMatcher.java`, with unit tests
in `RecipeMatcherTest.java`.

## Features
- Add, edit and delete pantry ingredients (name, quantity, unit, optional expiry date)
- Input validation on the ingredient form
- 20 recipes pre-loaded into the database on first launch
- Suggested Recipes screen (strict matches only, with a helpful empty-state message)
- Recipe detail screen (ingredients with quantities, and method)
- Settings screen (SharedPreferences): highlight ingredients expiring within 3 days
- Bottom navigation bar: Pantry, Recipes, Settings

## Database choice: SQLite
SQLite via `SQLiteOpenHelper` was chosen because it runs locally on the device, needs
no server, internet connection or API keys, is ideal for CRUD on a single-user app, and
matches the persistent-data approach taught in the module. Data persists after the app
is closed and reopened.

Tables: `pantry_item`, `recipe`, `recipe_ingredient` (foreign key to `recipe`).

## Project structure
- `PantryDBHelper`: creates tables and seeds recipes
- `PantryDataSource`: CRUD and read queries
- `PantryItem`, `Recipe`, `RecipeIngredient`: model classes
- `RecipeMatcher`: strict-matching logic
- `PantryAdapter`, `RecipeAdapter`: RecyclerView adapters
- Activities: `MainActivity` (pantry list), `AddEditIngredientActivity`,
  `SuggestedRecipesActivity`, `RecipeDetailActivity`, `SettingsActivity`

## Setup and run
1. Install Android Studio (Narwhal or later) with an Android SDK
2. Clone the repository: `git clone https://github.com/Someones-codes/Smart-Pantry-Manager.git`
3. Open the project folder in Android Studio and let Gradle sync finish
4. Create an emulator (Tools > Device Manager, e.g. Pixel 4a, API 30+) or plug in a device
5. Click Run. Recipes are seeded automatically on first launch

To run the unit tests, right-click `RecipeMatcherTest` and choose Run.

## Out of scope
No maps, GPS or location features; no payments.

## Author
Prince Chishanga, Richfield Graduate Institute