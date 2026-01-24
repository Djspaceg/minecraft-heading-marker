# Heading Marker - Quick Reference

## Installation

1. Copy `datapack/` to your world's `datapacks/` folder
2. Copy `resourcepack/` to your Minecraft `resourcepacks/` folder
3. Run `/reload` in-game

## Commands

```
/function heading_marker:add_marker
```
Shows instructions for adding a waypoint marker

```
/function heading_marker:remove_marker
```
Shows instructions for removing a waypoint marker

```
/function heading_marker:add_marker_coordinates
```
Shows advanced instructions for coordinate-based markers

## Quick Usage

1. **Get a compass:** `/give @s compass`
2. **Get a lodestone:** `/give @s lodestone`
3. **Place lodestone** at your waypoint location
4. **Right-click lodestone** with compass in hand
5. **Your compass now points to that waypoint!**

## Multiple Waypoints

- Each compass tracks ONE lodestone
- Create multiple compasses for multiple waypoints
- Use anvil to rename compasses for organization
- Place colored carpet on lodestone for visual coding

## Tips

- Label your compasses in an anvil
- Keep a home compass in your Ender Chest
- Store compasses in item frames at your base
- Lodestone compasses work across dimensions!

## Coordinates

Place lodestone at exact coordinates:
```
/execute positioned <x> <y> <z> run setblock ~ ~ ~ lodestone
```

Teleport to coordinates:
```
/tp @s <x> <y> <z>
```

## Version

- Minecraft: 1.21+
- Data Pack Format: 48
- Resource Pack Format: 34

## More Help

- See README.md for full documentation
- See INSTALLATION.md for detailed setup
- See USAGE.md for examples and use cases
