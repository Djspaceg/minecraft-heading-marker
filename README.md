# Heading Marker

A Minecraft Java Edition data pack that displays multiple custom waypoint markers on your HUD (actionbar) using slash commands. Track up to 5 waypoints simultaneously with colored icons and real-time distance tracking!

## Features

- 🎯 **Multiple HUD Markers** - Track up to 5 waypoints at once (one per color)
- 📍 **Coordinate-Based** - Enter 2D (X, Z) or 3D (X, Y, Z) coordinates via commands
- 🎨 **5 Color Options** - 🔴 Red, 🔵 Blue, 🟢 Green, 🟡 Yellow, 🟣 Purple
- 📏 **Distance Tracking** - See real-time distance² to all active waypoints
- 💾 **Persistence** - Markers saved and restored between gameplay sessions
- 🔄 **Auto-Color Cycling** - Automatically assigns next available color
- ⚡ **Instant Updates** - All markers update every tick (20 times per second)
- 👥 **Multiplayer Support** - Each player has their own set of 5 markers
- 🌍 **Cross-Dimension** - Works in Overworld, Nether, and End

## Quick Start

### Set a Marker (2D - Y defaults to 64)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y -500
/function heading_marker:marker_set
```
Result: `🔴 Red marker set at 1000 64 -500`

### Set a Marker (3D with specific coordinates)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/function heading_marker:marker_set
```
Result: `🔵 Blue marker set at 1000 64 -500` (auto-cycled to next color)

### Set with Specific Color
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/scoreboard players set @s hm.input.color 2  # 0=red, 1=blue, 2=green, 3=yellow, 4=purple
/function heading_marker:marker_set
```
Result: `🟢 Green marker set at 1000 64 -500`

### Remove a Marker
```
/scoreboard players set @s hm.input.color 1  # Remove blue marker
/function heading_marker:marker_remove
```
Result: `🔵 Blue marker removed`

### Actionbar Display
When markers are active, you'll see:
```
🔴245820 🔵180500 🟢0
```
(Shows emoji icon and distance² for each active marker)

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

#### Set Marker - 2D Mode (X, Z coordinates)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y -500
/function heading_marker:marker_set
```
In 2D mode, the second coordinate is Z, and Y defaults to 64.

#### Set Marker - 3D Mode (X, Y, Z coordinates)
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/function heading_marker:marker_set
```

#### Set Marker with Specific Color
```
/scoreboard players set @s hm.input.x 1000
/scoreboard players set @s hm.input.y 64
/scoreboard players set @s hm.input.z -500
/scoreboard players set @s hm.input.color 2  # 0=red, 1=blue, 2=green, 3=yellow, 4=purple
/function heading_marker:marker_set
```
If color is not specified, the system automatically cycles to the next available color.

#### Remove Marker
```
/scoreboard players set @s hm.input.color 1  # Specify which color to remove
/function heading_marker:marker_remove
```
Color is **required** for removal.

### How the HUD Works

When markers are active, you'll see on your actionbar:
```
🔴245820 🔵180500 🟢0 🟡5420 🟣980000
```

- **🔴🔵🟢🟡🟣** - Colored emoji icons for each active marker
- **Numbers** - Distance² to each waypoint (lower = closer)
- **Inactive markers** - Don't appear in the display

The display updates automatically 20 times per second as you move!

### Advanced Usage

#### Multiple Waypoints Simultaneously

You can have up to 5 markers active at once (one per color):
- 🔴 **Red** (0) - Home/Base
- 🔵 **Blue** (1) - Mines/Resources
- 🟢 **Green** (2) - Farms
- 🟡 **Yellow** (3) - Villages/Trading
- 🟣 **Purple** (4) - Nether Portals/End Portals

All markers are shown together on your HUD, allowing you to track multiple important locations at once!

#### Color Auto-Cycling

If you don't specify a color when setting a marker, the system:
1. Finds the next unused color
2. If all colors are used, cycles through in order (red → blue → green → yellow → purple → red)
3. Automatically assigns that color to your new marker

#### Persistence Between Sessions

Your markers are automatically saved and will be restored when you:
- Rejoin the world
- Restart the server
- Reload the data pack

No need to manually save - it happens automatically!

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
