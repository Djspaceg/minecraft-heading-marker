# Heading Marker

A Minecraft Java Edition waypoint system that displays custom markers on your HUD using vanilla Minecraft's waypoint system. **Works with vanilla clients - no mods required on the client side!**

## ✨ Key Features

- 🎯 **Vanilla Client Support** - No client mods needed! Uses Minecraft 1.21.11+ native waypoint system
- 🌍 **Server-Delivered** - Waypoints are sent from server to client over-the-wire
- 🎨 **5 Color Options** - 🔴 Red, 🔵 Blue, 🟢 Green, 🟡 Yellow, 🟣 Purple
- 📍 **Locator Bar Display** - Waypoints appear in your vanilla Locator Bar (above XP bar)
- 💾 **Per-Player Tracking** - Each player has their own waypoints
- 🔧 **Two Implementations** - Pure datapack OR optional Fabric mod for enhanced commands

## Quick Start (Vanilla Clients)

### Installation

**For Servers:**
1. Copy the `headingmarker/` folder to your world's `datapacks/` directory
2. Run `/reload` in-game
3. That's it! Vanilla clients can now use waypoints without any mods

**Commands:**
```mcfunction
/function headingmarker:set_macro {x:1000,y:64,z:-500}
/function headingmarker:set_here
/function headingmarker:remove
/function headingmarker:help
```

Waypoints will appear in your Locator Bar with distance indicators!

## Optional: Fabric Mod (Enhanced Commands)

For servers running Fabric, an optional mod provides shorter `/hm` commands:

```
/hm set <color>          # Set waypoint at your location
/hm set <x> <y> <z>      # Set waypoint at coordinates
/hm remove <color>       # Remove waypoint
/hm list                 # List your waypoints
/hm help                 # Show help
```

**Note:** The mod is server-side only. Vanilla clients work without it!

## Features

- 🎯 **Multiple HUD Markers** - Track up to 5 waypoints with colored icons
- 🌍 **Vanilla Client Compatible** - Works without any client-side mods using Minecraft 1.21.11+ native waypoint system
- 📍 **Locator Bar Display** - Waypoints appear in the vanilla Locator Bar (above XP bar) with distance indicators
- 🎨 **5 Color Options** - 🔴 Red, 🔵 Blue, 🟢 Green, 🟡 Yellow, 🟣 Purple
- 💾 **Per-Player Markers** - Each player has their own set of waypoints
- 🔄 **Auto-Persistence** - Waypoints persist across server restarts
- ⚡ **Real-Time Updates** - Distance and direction update automatically
- 🚀 **Two Options** - Use pure datapack for vanilla clients, or optional Fabric mod for enhanced commands

## How It Works

This project uses Minecraft 1.21.11+'s **native waypoint system**:

1. Datapack creates invisible armor stands at waypoint locations
2. Uses vanilla `/waypoint modify` command to make them trackable
3. Server sends waypoint data to clients automatically
4. Vanilla clients render waypoints in the Locator Bar - **no mods needed!**

## Installation

### Datapack Installation (Recommended for Vanilla Clients)

1. Download or clone this repository
2. Copy the `headingmarker` folder to your Minecraft world's `datapacks` directory:
   - Windows: `%appdata%\.minecraft\saves\[YourWorldName]\datapacks\`
   - Mac/Linux: `~/.minecraft/saves/[YourWorldName]/datapacks/`
3. The final path should be: `saves/[YourWorldName]/datapacks/headingmarker/pack.mcmeta`
4. Load or reload your world
5. Run `/reload` in-game to activate the data pack
6. You should see a message: "Heading Marker loaded! Use /function headingmarker:set to create a waypoint."

**Vanilla clients can now connect and use waypoints without any mods!**

### Optional: Fabric Mod Installation (Enhanced Commands)

If you want shorter `/hm` commands instead of `/function` commands:

1. Install Fabric Loader for Minecraft 1.21.11 on the **server only**
2. Drop the `headingmarker-1.0.x.jar` into your server's `mods` folder
3. Restart the server
4. Clients still don't need any mods!

The mod provides `/hm` commands while vanilla clients work normally.

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
4. The font file (`resourcepack/assets/headingmarker/font/default.json`) is already configured to map these sprites to unicode characters \uE000-\uE004

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

**Tab-completion support:** When typing `/function headingmarker:`, press Tab to see all available commands including:

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

