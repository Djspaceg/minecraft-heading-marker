# Heading Marker

A Minecraft Java Edition data pack that lets you create custom waypoint markers on your compass using slash commands. Navigate to any coordinates with ease using the built-in lodestone compass system enhanced with helpful commands!

## Features

- ✨ Add heading markers to your compass using simple commands
- 🧭 Works with Minecraft's native lodestone compass system
- 📍 Support for 2D (X, Z) or 3D (X, Y, Z) coordinates
- 🎨 Each marker can have unique colors (using carpet on lodestones)
- 📡 Full multiplayer support
- 🔧 Easy to use slash commands

## Installation

### Data Pack Installation

1. Download or clone this repository
2. Copy the `datapack` folder to your Minecraft world's `datapacks` directory:
   - Windows: `%appdata%\.minecraft\saves\[YourWorldName]\datapacks\`
   - Mac/Linux: `~/.minecraft/saves/[YourWorldName]/datapacks/`
3. Rename the folder to `heading-marker` (optional but recommended)
4. Load or reload your world
5. Run `/reload` in-game to activate the data pack

### Resource Pack Installation (Optional)

1. Copy the `resourcepack` folder to your Minecraft `resourcepacks` directory:
   - Windows: `%appdata%\.minecraft\resourcepacks\`
   - Mac/Linux: `~/.minecraft/resourcepacks/`
2. Rename the folder to `heading-marker-resources` (optional)
3. Enable the resource pack in Minecraft settings

## Usage

### Basic Commands

The data pack provides three main functions:

#### Add a Marker
```
/function heading_marker:add_marker
```
Displays instructions on how to create a waypoint marker using a lodestone and compass.

**Steps:**
1. Place a Lodestone at your desired waypoint location
2. Hold a Compass and right-click the Lodestone
3. Your compass will now point to that location!

**Tip:** Place colored carpet on the lodestone before linking to color-code your waypoints!

#### Add Marker at Specific Coordinates
```
/function heading_marker:add_marker_coordinates
```
Shows advanced instructions for creating a marker at specific coordinates using commands.

#### Remove a Marker
```
/function heading_marker:remove_marker
```
Displays instructions on how to remove waypoint markers.

**Options:**
- Break the Lodestone - compass will spin randomly again
- Use an anvil to clear lodestone tracking
- Drop or destroy the lodestone compass

### Advanced Usage

You can also use direct Minecraft commands for more control:

#### Place a lodestone at specific coordinates:
```
/execute positioned <x> <y> <z> run setblock ~ ~ ~ lodestone
```

#### Get a compass:
```
/give @s compass
```

#### Teleport to coordinates to link your compass:
```
/tp @s <x> <y> <z>
```

## How It Works

This data pack leverages Minecraft's built-in lodestone compass mechanics:
- Lodestone compasses point to a specific lodestone block
- Breaking the lodestone causes the compass to spin randomly
- Each compass can only track one lodestone at a time
- Multiple compasses can track different lodestones for multiple waypoints

The data pack adds helpful slash commands to make working with these mechanics easier and more intuitive!

## Compatibility

- **Minecraft Version:** 1.21+ (Data Pack Format 48)
- **Game Mode:** Survival, Creative, Adventure
- **Multiplayer:** ✅ Fully supported
- **Server:** ✅ Works on vanilla servers

## Technical Details

### Data Pack Structure
```
datapack/
├── pack.mcmeta                           # Data pack metadata
└── data/
    ├── heading_marker/
    │   └── functions/
    │       ├── load.mcfunction          # Initialization
    │       ├── add_marker.mcfunction    # Add marker command
    │       ├── add_marker_coordinates.mcfunction  # Advanced add
    │       └── remove_marker.mcfunction # Remove marker command
    └── minecraft/
        └── tags/
            └── functions/
                └── load.json            # Load tag
```

### Resource Pack Structure
```
resourcepack/
├── pack.mcmeta                          # Resource pack metadata
└── assets/
    └── heading_marker/
        └── textures/
            └── gui/
                └── sprites/
                    └── hud/             # Custom HUD icons (future)
```

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
