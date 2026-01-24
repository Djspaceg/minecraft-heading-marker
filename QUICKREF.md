# Heading Marker - Quick Reference

## Installation

1. Copy `datapack/` to your world's `datapacks/` folder
2. Run `/reload` in-game
3. Use commands to set markers - they'll appear on your actionbar!

## Commands

### Set Marker (2D - Y defaults to 64)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y -500
/function heading_marker:marker_set
```

### Set Marker (3D)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/function heading_marker:marker_set
```

### Set Marker with Specific Color
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/scoreboard players set @s hm.input.color 2  # 0=red, 1=blue, 2=green, 3=yellow, 4=purple
/function heading_marker:marker_set
```

### Remove Marker (color required)
```
/scoreboard players set @s hm.input.color 1  # Remove blue marker
/function heading_marker:marker_remove
```

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
- Resource Pack Format: 34 (optional/future)

## More Help

- See README.md for full documentation
- See INSTALLATION.md for detailed setup
