# Heading Marker Mod (Fabric)

A Fabric mod for Minecraft 26.3 that gives each player their own waypoints, shown in the vanilla
Locator Bar with live distances on the actionbar.

## Features

- **Per-Player Waypoints:** Each player places as many waypoints as they like in red, blue, green,
  yellow, or purple. Other players can't see them unless they're shared.
- **Locator Bar + Distances:** Waypoints appear in the vanilla Locator Bar (direction). The
  actionbar shows the distance to each one, e.g. `🔴 Red 245  🔵 Home 180`.
- **Names and Keys:** Every waypoint gets an 8-character key. You can also give it a name.
- **Dimension Aware:** Waypoints belong to the dimension they were set in. Commands act on your
  current dimension.
- **Persistent Storage:** Each world keeps its own waypoints in
  `<world>/headingmarker/<player-uuid>.json`, saved whenever the world saves. Earlier versions
  used one shared `waypoints/` folder in the game directory; its files are copied into each
  world the first time that world is opened.
- **Server-Side:** Vanilla clients can connect to a server running the mod.

## Commands

`/headingmarker` is an alias for `/hm`. Every command except `purge` is available to all players.

- `/hm` or `/hm help` - Show in-game help.
- `/hm set [color] [x z | x y z]` - Place a waypoint.
    - With no coordinates it uses your position. With `x z` it uses your current Y.
    - The color can also go after the coordinates: `/hm set 100 64 200 red`.
    - With no color it picks the one you've used least in this dimension.
    - Colors: red, blue, green, yellow, purple.
- `/hm list` - List your waypoints in this dimension, with their keys.
- `/hm rename <selector> [name]` - Name matching waypoints, or clear the name if you leave it out.
  Quote a multi-word selector: `/hm rename "Home Base" Base`. Quotes around a selector or name
  are optional everywhere else.
- `/hm remove <selector>` - Remove matching waypoints.
- `/hm share <player> <selector>` - Give an online player copies of matching waypoints. The copies
  are placed in the same dimension.
- `/hm clear` - Remove all your waypoints in this dimension.
- `/hm clearall` - Remove all your waypoints in every dimension.
- `/hm purge` - Operators only (permission level 2). Remove leftover marker entities that are
  currently loaded. Leftovers are also removed automatically when their chunk loads.

A `<selector>` is a marker key, a color, or a name. It matches every waypoint in your current
dimension that fits, so `/hm remove red` removes all your red waypoints there. Keys match exactly;
colors and names ignore case.

## Building

This project uses Gradle and Java 25.

1. Open a terminal in this folder.
2. Run `./gradlew build` (Linux/Mac) or `gradlew build` (Windows).
3. The compiled `.jar` file will be in `build/libs/`.

## Installation

1. Install Fabric Loader, Fabric API, and Fabric Language Kotlin for Minecraft 26.3.
2. Drop `minecraft-heading-marker-<version>.jar` into your `mods` folder.
3. Restart the game or server.

## Data Pack

The standalone data pack version of this project is in `datapack_for_headingmarker/`.
