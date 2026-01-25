# Heading Marker Mod (Fabric)

This is a Fabric mod for Minecraft 1.21.1 that provides per-player waypoints and a HUD compass/distance indicator.

## Features

- **Per-Player Waypoints:** Each player can set their own Red, Blue, Green, Yellow, and Purple waypoints.
- **HUD Indicator:** Shows the distance and direction to your active waypoints in the Action Bar.
- **Persistent Storage:** Waypoints are saved to the server/world data and persist across restarts.
- **Dimension Aware:** Waypoints mark the dimension they were set in.

## Commands

- `/hm set <color>` - Set a waypoint at your current location.
  - Colors: red, blue, green, yellow, purple
- `/hm remove <color>` - Remove an existing waypoint.
- `/hm list` - List your current waypoints and coordinates.

## Building

This project uses Gradle.

1.  Open a terminal in this folder.
2.  Run `./gradlew build` (Linux/Mac) or `gradlew build` (Windows).
3.  The compiled `.jar` file will be in `build/libs/`.

## Installation

1.  Install Fabric Loader for Minecraft 1.21.1.
2.  Drop the `headingmarker-1.0.0.jar` into your `mods` folder.
3.  Restart the game.

## Legacy

The original datapack version of this project has been moved to `legacy_datapack/`.
