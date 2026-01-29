# Waypoint Marker Data Pack

This Minecraft data pack allows players to track and display arbitrary coordinates using the `/wp` command.


## Features

- `/wp <name> <x> <y> <z>`: Save a waypoint with a custom name and coordinates.
- `/function waypointmarker:display_waypoints`: Display all saved waypoints for the player.

## Installation

1. Copy the `waypointmarker` folder into your world's `datapacks` directory.
2. Run `/reload` in-game.
3. Use `/function waypointmarker:wp <name> <x> <y> <z>` to add a waypoint.
4. Use `/function waypointmarker:display_waypoints` to view your waypoints.

## Notes

- This is a minimal demonstration. For production use, add error handling and per-player storage iteration.
