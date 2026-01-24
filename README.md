# Heading Marker

A Minecraft Java Edition data pack that displays custom waypoint markers on your HUD (actionbar) using slash commands. Enter coordinates and see a colored icon appear on your screen showing the waypoint location and distance!

## Features

- 🎯 **HUD Waypoint Markers** - Colored icons appear on your actionbar
- 📍 **Coordinate-Based** - Enter 2D (X, Z) or 3D (X, Y, Z) coordinates via commands
- 🎨 **5 Color Options** - Red, Blue, Green, Yellow, or Purple markers
- 📏 **Distance Tracking** - See real-time distance to your waypoint
- ⚡ **Instant Updates** - Marker updates every tick (20 times per second)
- 👥 **Multiplayer Support** - Each player has their own personal marker
- 🌍 **Cross-Dimension** - Works in Overworld, Nether, and End

## Quick Start

1. **Mark your current location:**
   ```
   /function heading_marker:set_marker
   ```

2. **Or set specific coordinates:**
   ```
   /scoreboard players set @s hm.x 1000
   /scoreboard players set @s hm.y 64
   /scoreboard players set @s hm.z -500
   /function heading_marker:set_marker_at
   ```

3. **See the marker on your actionbar!**
   - A colored icon (🔴🔵🟢🟡🟣) appears
   - Shows waypoint coordinates
   - Displays distance² to target

4. **Change marker color:**
   ```
   /function heading_marker:set_color_blue
   ```
   (Options: red, blue, green, yellow, purple)

5. **Clear the marker:**
   ```
   /function heading_marker:clear_marker
   ```

## Installation

### Data Pack Installation

1. Download or clone this repository
2. Copy the `datapack` folder to your Minecraft world's `datapacks` directory:
   - Windows: `%appdata%\.minecraft\saves\[YourWorldName]\datapacks\`
   - Mac/Linux: `~/.minecraft/saves/[YourWorldName]/datapacks/`
3. Rename the folder to `heading-marker` (optional but recommended)
4. Load or reload your world
5. Run `/reload` in-game to activate the data pack
6. You should see a welcome message confirming the data pack loaded

### Resource Pack (Future Enhancement)

The resource pack structure is prepared for future custom icon textures. Currently, the HUD uses emoji icons (🔴🔵🟢🟡🟣) which work without a resource pack.

## Usage

### Main Commands

#### Set Marker at Current Location
```
/function heading_marker:set_marker
```
Marks your current position as the waypoint. The marker immediately appears on your actionbar.

#### Set Marker at Specific Coordinates
```
/function heading_marker:help_coordinates
```
Shows clickable commands to set exact coordinates:
1. Set X: `/scoreboard players set @s hm.x <value>`
2. Set Y: `/scoreboard players set @s hm.y <value>`
3. Set Z: `/scoreboard players set @s hm.z <value>`
4. Activate: `/function heading_marker:set_marker_at`

**Example - Set marker at X=1000, Y=64, Z=-500:**
```
/scoreboard players set @s hm.x 1000
/scoreboard players set @s hm.y 64
/scoreboard players set @s hm.z -500
/function heading_marker:set_marker_at
```

#### Change Marker Color
```
/function heading_marker:set_color_red
/function heading_marker:set_color_blue
/function heading_marker:set_color_green
/function heading_marker:set_color_yellow
/function heading_marker:set_color_purple
```

#### Clear Marker
```
/function heading_marker:clear_marker
```
Removes the waypoint marker from your HUD.

#### Get Help
```
/function heading_marker:add_marker
```
Shows all available commands and usage instructions.

### How the HUD Works

When a marker is active, you'll see on your actionbar:
```
🔴 Waypoint: 1000 64 -500 | Distance²: 245820
```

- **🔴** - Colored icon (changes based on your color selection)
- **Waypoint: X Y Z** - The target coordinates
- **Distance²** - Squared distance to target (lower = closer)

The display updates automatically 20 times per second as you move!

### Advanced Usage

#### Multiple Waypoints (Per Player)

Each player can have one active marker at a time. To track multiple locations:
- Take note of coordinates before clearing
- Switch between waypoints as needed
- Or share coordinates with teammates

#### Understanding Distance²

The distance shown is squared (distance²) for performance:
- **0-100**: Very close (0-10 blocks)
- **100-10,000**: Close range (10-100 blocks)
- **10,000-1,000,000**: Medium range (100-1000 blocks)
- **1,000,000+**: Far away (1000+ blocks)

To get actual distance, take the square root of the displayed value.

#### Color Coding Your Waypoints

Use different colors for different waypoint types:
- 🔴 **Red** - Home/Base
- 🔵 **Blue** - Mines/Resources
- 🟢 **Green** - Farms
- 🟡 **Yellow** - Villages/Trading
- 🟣 **Purple** - Nether Portals/End Portals

#### Multiplayer Coordination

On multiplayer servers:
- Each player's marker is personal (others can't see it)
- Share coordinates in chat to help teammates
- Use consistent color codes as a team
- Great for treasure hunts and group exploration!

## How It Works

This data pack uses Minecraft's built-in scoreboard and title/actionbar systems:
- Scoreboards store waypoint coordinates per player
- A tick function (runs 20x/second) calculates your distance
- The actionbar displays the marker with real-time updates
- No mods or external tools required!

The system is 100% vanilla Minecraft - it works on any server running the data pack.

## Compatibility

- **Minecraft Version:** 1.21+ (Data Pack Format 48)
- **Game Mode:** Survival, Creative, Adventure
- **Multiplayer:** ✅ Fully supported
- **Server:** ✅ Works on vanilla servers

## Technical Details

### Data Pack Structure
```
datapack/
├── pack.mcmeta                              # Data pack metadata (format 48)
└── data/
    ├── heading_marker/
    │   └── functions/
    │       ├── load.mcfunction              # Initialization & scoreboards
    │       ├── tick.mcfunction              # Updates HUD every tick
    │       ├── display_hud.mcfunction       # Renders actionbar display
    │       ├── set_marker.mcfunction        # Mark current location
    │       ├── set_marker_at.mcfunction     # Mark specific coordinates
    │       ├── clear_marker.mcfunction      # Remove marker
    │       ├── set_color_*.mcfunction       # Change marker color (5 colors)
    │       ├── help_coordinates.mcfunction  # Coordinate entry help
    │       ├── add_marker.mcfunction        # Legacy/help function
    │       ├── add_marker_coordinates.mcfunction  # Legacy redirect
    │       └── remove_marker.mcfunction     # Legacy redirect
    └── minecraft/
        └── tags/
            └── functions/
                ├── load.json                # Auto-load on world start
                └── tick.json                # Run every tick (20x/sec)
```

### Scoreboard Objectives
- `hm.x`, `hm.y`, `hm.z` - Waypoint coordinates
- `hm.color` - Marker color (0-4)
- `hm.active` - Whether marker is enabled
- `hm.dx`, `hm.dz` - Delta calculations
- `hm.dist` - Distance² to waypoint
- `hm.temp` - Temporary calculations

### Resource Pack Structure
```
resourcepack/
├── pack.mcmeta                              # Resource pack metadata (format 34)
└── assets/
    └── heading_marker/
        └── textures/
            └── gui/
                └── sprites/
                    └── hud/                 # Custom HUD icons (future)
```

**Note:** The resource pack is currently a placeholder. The HUD uses emoji icons (🔴🔵🟢🟡🟣) which work without a resource pack.

## Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest new features
- Submit pull requests
- Improve documentation

## License

This project is open source and available under the MIT License.

## Credits

Created for Minecraft Java Edition players who want better waypoint navigation!
