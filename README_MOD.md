# Heading Marker Mod (Fabric - Server-Only)

**Note:** This mod is **optional** and **server-side only**. For vanilla client support, use the datapack in `/headingmarker/` instead!

This is a Fabric mod for Minecraft 1.21.11 that provides enhanced `/hm` commands for waypoint management. It works on the server only - vanilla clients can connect without any mods.

## Key Points

- ✅ **Server-Side Only** - Install on server, clients don't need it
- ✅ **Vanilla Client Compatible** - Works with unmodded clients
- ✅ **Optional** - The datapack provides full functionality without this mod
- ✅ **Enhanced Commands** - Provides `/hm` shortcuts instead of `/function` commands

## Features

- **Shorter Commands:** Use `/hm` instead of `/function headingmarker:...`
- **Per-Player Waypoints:** Each player can set their own waypoints
- **Persistent Storage:** Waypoints are saved server-side and persist across restarts
- **Dimension Aware:** Works across Overworld, Nether, and End

## Commands

- `/hm set <color>` - Set a waypoint at your current location
  - Colors: red, blue, green, yellow, purple
- `/hm set <x> <z>` - Set a waypoint at 2D coordinates (Y = player position)
- `/hm set <x> <y> <z>` - Set a waypoint at 3D coordinates
- `/hm remove <color>` - Remove an existing waypoint
- `/hm list` - List your current waypoints and coordinates
- `/hm showdistance yes|no` - Toggle distance display (requires custom client-side implementation)
- `/hm help` - Show command help

## Installation

### Server Installation

1. Install Fabric Loader for Minecraft 1.21.11 on the **server only**
2. Drop the `headingmarker-1.0.x.jar` into your server's `mods` folder
3. (Recommended) Also install the datapack for waypoint rendering
4. Restart the server
5. Clients can connect with vanilla Minecraft - no mods needed!

### Client Installation

**NOT REQUIRED!** Vanilla clients work without any mods.

The mod is server-side only. Waypoints are rendered using Minecraft's native waypoint system, which vanilla clients support automatically.

## Building

This project uses Gradle.

1. Open a terminal in this folder
2. Run `./gradlew build` (Linux/Mac) or `gradlew build` (Windows)
3. The compiled `.jar` file will be in `build/libs/`

## Datapack vs Mod

### Use the Datapack When:
- You want vanilla clients to work without any setup
- You're running a vanilla or non-Fabric server
- You want the simplest installation

### Use the Mod When:
- You're already running a Fabric server
- You prefer `/hm` commands over `/function` commands
- You want persistent waypoint storage managed by the mod

**Both can be used together!**

## Mod vs Datapack Rendering

- The **datapack** handles waypoint rendering using vanilla waypoint entities and the `waypoint modify` command
- The **mod** only provides commands and server-side storage
- Vanilla clients receive waypoint data automatically and render them in the Locator Bar

## Legacy

The previous version of this mod included client-side rendering code that required Fabric API on clients. This has been removed to support vanilla clients.
