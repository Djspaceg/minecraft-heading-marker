# Heading Marker - Quick Reference

## Installation

1. Copy `datapack/` to your world's `datapacks/` folder
2. Run `/reload` in-game
3. See the colored marker appear on your actionbar!

## Commands

### Set Marker at Current Location
```
/function heading_marker:set_marker
```

### Set Marker at Specific Coordinates
```
/scoreboard players set @s hm.x <x_value>
/scoreboard players set @s hm.y <y_value>
/scoreboard players set @s hm.z <z_value>
/function heading_marker:set_marker_at
```

### Change Marker Color
```
/function heading_marker:set_color_red
/function heading_marker:set_color_blue
/function heading_marker:set_color_green
/function heading_marker:set_color_yellow
/function heading_marker:set_color_purple
```

### Clear Marker
```
/function heading_marker:clear_marker
```

### Get Help
```
/function heading_marker:add_marker
/function heading_marker:help_coordinates
```

## Quick Example

**Mark home at current position:**
```
/function heading_marker:set_marker
```

**Set waypoint to X=1000, Y=64, Z=-500:**
```
/scoreboard players set @s hm.x 1000
/scoreboard players set @s hm.y 64
/scoreboard players set @s hm.z -500
/function heading_marker:set_marker_at
```

**Change to blue marker:**
```
/function heading_marker:set_color_blue
```

## HUD Display

When active, your actionbar shows:
```
🔴 Waypoint: 1000 64 -500 | Distance²: 245820
```

- Colored icon based on your selection
- Target coordinates
- Distance² to waypoint (lower = closer)
- Updates 20 times per second

## Color Guide

- 🔴 Red - Home/Base
- 🔵 Blue - Mines/Resources  
- 🟢 Green - Farms
- 🟡 Yellow - Villages
- 🟣 Purple - Portals

## Tips

- One marker per player at a time
- Distance² shown (square root to get actual distance)
- Works in all dimensions
- Markers are personal (others can't see yours)
- Share coordinates in chat with teammates

## Version

- Minecraft: 1.21+
- Data Pack Format: 48
- Resource Pack Format: 34 (optional/future)

## More Help

- See README.md for full documentation
- See INSTALLATION.md for detailed setup
- See USAGE.md for examples and use cases
- See TUTORIAL.md for step-by-step guide
