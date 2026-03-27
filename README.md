# Heading Marker

A Minecraft Java Edition data pack that displays multiple custom waypoint markers on your HUD (
actionbar) using slash commands. Track up to 5 waypoints per dimension with colored icons and
real-time distance tracking!

## Features

- 🎯 **Multiple HUD Markers** - Track up to 5 waypoints per dimension (15 total across all
  dimensions)
- 🌍 **Per-Dimension Markers** - Separate marker sets for Overworld, Nether, and End
- 📍 **Coordinate-Based** - Enter 2D (X, Z) or 3D (X, Y, Z) coordinates via commands
- 🎨 **5 Color Options** - 🔴 Red, 🔵 Blue, 🟢 Green, 🟡 Yellow, 🟣 Purple
- 📏 **Distance Tracking** - See real-time distance² to all active waypoints in current dimension
- 💾 **Full Persistence** - Markers saved and restored between gameplay sessions with UUID-based
  per-player per-dimension storage
- 🔄 **Auto-Color Cycling** - Automatically assigns next available color
- ⚡ **Instant Updates** - All markers update every tick (20 times per second)
- 👥 **Per-Player Markers** - Each player has their own set of markers (5 per dimension) with
  UUID-based persistence for multiplayer servers
- 🚪 **Dimension-Aware** - Automatically saves/loads correct markers when changing dimensions
- 💡 **Built-in Help** - In-game help command with clickable examples and tab-completion

## Quick Start

### Get Help In-Game

```
/function headingmarker:help
```

Shows all commands with clickable examples that you can edit and use!

### Set a Marker (2D - Y defaults to 64)

```
/function headingmarker:set_2d {x:1000, z:-500}
```

Result: `🔴 Red marker set at 1000 64 -500`

### Set a Marker (3D with specific coordinates)

```
/function headingmarker:set {x:1000, y:64, z:-500}
```

Result: `🔵 Blue marker set at 1000 64 -500` (auto-cycled to next color)

### Set with Specific Color

```
/function headingmarker:set_3d_color {x:1000, y:64, z:-500, color:2}
```

Result: `🟢 Green marker set at 1000 64 -500`

Colors: `0=red, 1=blue, 2=green, 3=yellow, 4=purple`

### Remove a Marker

```
/function headingmarker:remove {color:1}
```

Result: `🔵 Blue marker removed`

### Actionbar Display

When markers are active, you'll see:

```
🔴245820 🔵180500 🟢0
```

(Shows emoji icon and distance² for each active marker)

### Per-Dimension Markers

Markers are **dimension-specific**:

- **Overworld**: 5 markers (one per color)
- **Nether**: 5 markers (one per color)
- **End**: 5 markers (one per color)

When you change dimensions:

- Your current dimension's markers are automatically saved
- The new dimension's markers are automatically loaded
- You'll see a message: "Switched to [Dimension] markers"

This means you can have up to **15 total markers** (5 per dimension × 3 dimensions)!

**Example:**

1. In Overworld: Set red marker at spawn (0, 64, 0)
2. Go to Nether: Set red marker at your portal (-100, 70, 50)
3. Return to Overworld: You'll see your spawn marker again
4. Go back to Nether: You'll see your portal marker again

Each dimension maintains its own independent set of markers.

## Installation

### Data Pack Installation

1. Download or clone this repository
2. Copy the `headingmarker` folder to your Minecraft world's `datapacks` directory:
    - Windows: `%appdata%\.minecraft\saves\[YourWorldName]\datapacks\`
    - Mac/Linux: `~/.minecraft/saves/[YourWorldName]/datapacks/`
3. The final path should be: `saves/[YourWorldName]/datapacks/headingmarker/pack.mcmeta`
4. Load or reload your world
5. Run `/reload` in-game to activate the data pack
6. You should see a welcome message: "Heading Marker loaded! Use /function headingmarker:help for
   commands"

### Resource Pack (Optional Custom Sprites)

The resource pack includes a pre-configured font file for custom marker sprites.

**Current Status:** The HUD uses emoji icons (🔴🔵🟢🟡🟣) which work without a resource pack.

**To Add Custom Sprites:**

1. Create 16x16 pixel PNG images for each marker color
2. Place them in `resourcepack/assets/headingmarker/textures/hud/`:
    - `marker_red.png`
    - `marker_blue.png`
    - `marker_green.png`
    - `marker_yellow.png`
    - `marker_purple.png`
3. Install the resource pack in your world
4. The font file (`resourcepack/assets/headingmarker/font/default.json`) is already configured to
   map these sprites to unicode characters \uE000-\uE004

**Note:** You can use the data pack without the resource pack. Sprites are optional!

## Usage

### Main Commands

#### Set Marker - 2D Mode (X, Z coordinates)

```
/function headingmarker:set {x:1000, z:-500}
```

In 2D mode, Y defaults to 64.

#### Set Marker - 3D Mode (X, Y, Z coordinates)

```
/function headingmarker:set {x:1000, y:64, z:-500}
```

#### Set Marker with Specific Color

```
/function headingmarker:set {x:1000, y:64, z:-500, color:2}
```

Colors: `0=red, 1=blue, 2=green, 3=yellow, 4=purple`

If color is not specified, the system automatically cycles to the next available color.

#### Remove Marker

```
/function headingmarker:remove {color:1}
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

All markers are shown together on your HUD, allowing you to track multiple important locations at
once!

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

**Persistence System:**

- **UUID-Based Storage**: Each player's markers are saved separately using their unique player UUID
- **Multiplayer-Safe**: Works correctly for any number of players on multiplayer servers
- **Automatic Loading**: Markers are automatically loaded when players join the server or world
- **Per-Player**: Each player's markers persist independently without interfering with others

No need to manually save - it happens automatically!

#### In-Game Help and Tab-Completion

Get interactive help at any time:

```
/function headingmarker:help
```

This shows:

- ✅ All available commands with clickable examples
- ✅ Color reference guide
- ✅ Commands you can click to copy and edit
- ✅ Quick example functions for common use cases

**Tab-completion support:** When typing `/function headingmarker:`, press Tab to see all available
commands including:

- `set` - Set a marker
- `remove` - Remove a marker
- `help` - Show help
- `examples/home` - Example for home base
- `examples/mine` - Example for mines
- `examples/farm` - Example for farms
- `examples/village` - Example for villages
- `examples/portal` - Example for portals

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
headingmarker/                              # Main data pack folder (goes in datapacks/)
├── pack.mcmeta                              # Data pack metadata (format 48)
└── data/
    ├── headingmarker/
    │   └── functions/
    │       ├── load.mcfunction              # Initialization & scoreboards
    │       ├── tick.mcfunction              # Updates HUD every tick
    │       ├── display_all_markers.mcfunction # Renders actionbar display
    │       ├── set.mcfunction               # Set marker (3D mode)
    │       ├── set_2d.mcfunction            # Set marker (2D mode, Y=64)
    │       ├── set_2d_color.mcfunction      # Set marker (2D with color)
    │       ├── set_3d_color.mcfunction      # Set marker (3D with color)
    │       ├── remove.mcfunction            # Remove marker command
    │       ├── help.mcfunction              # In-game help system
    │       ├── save_markers.mcfunction      # Persistence (save)
    │       ├── load_markers.mcfunction      # Persistence (load)
    │       ├── examples/                    # Example marker templates
    │       │   ├── home.mcfunction
    │       │   ├── mine.mcfunction
    │       │   ├── farm.mcfunction
    │       │   ├── village.mcfunction
    │       │   └── portal.mcfunction
    │       └── internal/                    # Helper functions
    │           ├── set_red/blue/green/yellow/purple.mcfunction
    │           ├── remove_red/blue/green/yellow/purple.mcfunction
    │           ├── calc_red/blue/green/yellow/purple.mcfunction
    │           ├── auto_select_color.mcfunction
    │           ├── set_marker_2d.mcfunction
    │           ├── set_marker_3d.mcfunction
    │           ├── show_actionbar.mcfunction
    │           ├── show_multi.mcfunction
    │           ├── save_player_uuid.mcfunction
    │           ├── load_player_uuid.mcfunction
    │           └── ... (dimension-aware helpers)
    └── minecraft/
        └── tags/
            └── functions/
                ├── load.json                # Auto-load on world start
                └── tick.json                # Run every tick (20x/sec)
```

### Scoreboard Objectives (per player, per color)

- `hm.red.x/y/z`, `hm.red.active`, `hm.red.dist` - Red marker
- `hm.blue.x/y/z`, `hm.blue.active`, `hm.blue.dist` - Blue marker
- `hm.green.x/y/z`, `hm.green.active`, `hm.green.dist` - Green marker
- `hm.yellow.x/y/z`, `hm.yellow.active`, `hm.yellow.dist` - Yellow marker
- `hm.purple.x/y/z`, `hm.purple.active`, `hm.purple.dist` - Purple marker
- `hm.input.x/y/z/color` - Command input variables
- `hm.nextcolor` - Auto-cycling tracker
- `hm.dx`, `hm.dz`, `hm.dist`, `hm.temp` - Calculation variables

### Resource Pack Structure

```
resourcepack/
├── pack.mcmeta                              # Resource pack metadata (format 34)
└── assets/
    └── headingmarker/
        └── textures/
            └── gui/
                └── sprites/
                    └── hud/                 # Custom HUD icons (future)
```

**Note:** The resource pack is currently a placeholder. The HUD uses emoji icons (🔴🔵🟢🟡🟣) which work
without a resource pack.

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

