# Heading Marker - Quick Reference

## Installation

1. Copy `datapack/` to your world's `datapacks/` folder
2. Run `/reload` in-game
3. Use commands to set markers - they'll appear on your actionbar!

## Get Help In-Game

```
/function heading_marker:help
```
Shows all commands with clickable examples!

## Commands

### Set Marker (2D - Y defaults to 64)
```
/function heading_marker:set {x:1000, z:-500}
```

### Set Marker (3D)
```
/function heading_marker:set {x:1000, y:64, z:-500}
```

### Set Marker with Specific Color
```
/function heading_marker:set {x:1000, y:64, z:-500, color:2}
```
Colors: `0=red, 1=blue, 2=green, 3=yellow, 4=purple`

### Remove Marker
```
/function heading_marker:remove {color:1}
```

## Example Functions

Quick examples you can run and modify:
```
/function heading_marker:examples/home     # Red marker for home
/function heading_marker:examples/mine     # Blue marker for mine
/function heading_marker:examples/farm     # Green marker for farm
/function heading_marker:examples/village  # Yellow marker for village
/function heading_marker:examples/portal   # Purple marker for portal
```

## Tab-Completion

Press Tab after typing `/function heading_marker:` to see all available commands!

## HUD Display

When markers are active, your actionbar shows:
```
🔴245820 🔵180500 🟢0
```

- Colored emoji icons for each active marker
- Numbers show distance² to each waypoint (lower = closer)
- Up to 5 markers shown simultaneously

## Color Guide

- 🔴 Red (0) - Home/Base
- 🔵 Blue (1) - Mines/Resources  
- 🟢 Green (2) - Farms
- 🟡 Yellow (3) - Villages
- 🟣 Purple (4) - Portals

## Features

- Up to 5 simultaneous markers per player (one per color)
- Auto-cycles to next available color if not specified
- Markers persist between gameplay sessions
- Works in all dimensions
- Personal per-player markers

## Version

- Minecraft: 1.21+
- Data Pack Format: 48
- Uses macros (added in Minecraft 1.20.2)

## More Help

- See README.md for full documentation
- See INSTALLATION.md for detailed setup
