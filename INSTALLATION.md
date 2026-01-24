# Installation Guide

This guide will walk you through installing the Heading Marker data pack and resource pack for Minecraft Java Edition.

## Prerequisites

- Minecraft Java Edition 1.21 or later
- A Minecraft world (singleplayer or multiplayer with operator permissions)

## Step-by-Step Installation

### Installing the Data Pack

1. **Locate your Minecraft saves folder:**
   - **Windows:** Press `Win + R`, type `%appdata%\.minecraft\saves` and press Enter
   - **macOS:** Press `Cmd + Shift + G` in Finder, type `~/Library/Application Support/minecraft/saves` and press Enter
   - **Linux:** Navigate to `~/.minecraft/saves`

2. **Find your world folder:**
   - Look for the folder with your world's name
   - Open that folder

3. **Navigate to the datapacks folder:**
   - Inside your world folder, find the `datapacks` folder
   - If it doesn't exist, create it

4. **Install the data pack:**
   - Copy the entire `heading_marker` folder from this repository
   - Paste it into your world's `datapacks` folder
   - The final path should be: `saves/[YourWorldName]/datapacks/heading_marker/`

5. **Activate the data pack:**
   - If the world is already open, type `/reload` in chat
   - If the world is closed, simply open it
   - You should see a welcome message in chat confirming the data pack loaded

### Installing the Resource Pack (Optional)

The resource pack is optional but provides custom icons for your waypoint markers.

1. **Locate your Minecraft resource packs folder:**
   - **Windows:** Press `Win + R`, type `%appdata%\.minecraft\resourcepacks` and press Enter
   - **macOS:** Press `Cmd + Shift + G` in Finder, type `~/Library/Application Support/minecraft/resourcepacks` and press Enter
   - **Linux:** Navigate to `~/.minecraft/resourcepacks`

2. **Install the resource pack:**
   - Copy the entire `resourcepack` folder from this repository
   - Paste it into your `resourcepacks` folder
   - Optionally, rename it to `heading_marker-resources` for clarity

3. **Activate the resource pack:**
   - Open Minecraft
   - Go to Options → Resource Packs
   - Find "Heading Marker" in the available resource packs
   - Click the arrow to move it to "Selected Resource Packs"
   - Click "Done"

## Verification

To verify the installation was successful:

1. Join your world
2. Type `/function heading_marker:help` in chat
3. You should see help information and instructions for using Heading Marker

If you see the help message, congratulations! The data pack is installed correctly.

## Troubleshooting

### Data Pack Not Loading

**Problem:** No welcome message appears when joining the world

**Solutions:**
- Make sure the `heading_marker` folder is inside `[YourWorldName]/datapacks/`
- Check that `pack.mcmeta` is directly inside the data pack folder: `datapacks/heading_marker/pack.mcmeta`
- The folder structure should be: `datapacks/heading_marker/data/heading_marker/functions/...`
- Verify your Minecraft version is 1.21 or later (requires macro support from 1.20.2+)
- Try running `/reload` in-game
- Check for error messages in the chat or game output log

### Data Pack Shows as Incompatible

**Problem:** Data pack appears red/incompatible in the data pack list

**Solutions:**
- Your Minecraft version might be older than 1.21
- Download a version of the data pack compatible with your Minecraft version
- Check the `pack_format` number in `pack.mcmeta`

### Commands Don't Work

**Problem:** Commands aren't recognized or don't work

**Solutions:**
- Make sure you have operator permissions (single-player: enabled by default)
- On multiplayer servers, ask an admin to give you operator permissions
- Verify the data pack is enabled: `/datapack list`
- Reload the data pack: `/reload`

### Resource Pack Not Showing Custom Icons

**Problem:** Icons don't appear or look wrong

**Solutions:**
- Make sure the resource pack is enabled in Options → Resource Packs
- Icons may need to be created - check the resource pack's textures folder
- Try reloading resource packs: Press `F3 + T` in-game
- Restart Minecraft completely

## Multiplayer Servers

### Server Installation

1. Stop your Minecraft server
2. Navigate to your server's world folder
3. Follow the same data pack installation steps as above
4. Start your server
5. The data pack should load automatically

### Permissions

Players need operator permissions to use the function commands. Server admins can grant this with:
```
/op <playername>
```

Alternatively, you could create a command block system to allow non-op players to use the features.

## Uninstallation

### Removing the Data Pack

1. Navigate to your world's `datapacks` folder
2. Delete the `heading_marker` folder
3. Run `/reload` in-game or restart the world

### Removing the Resource Pack

1. Go to Options → Resource Packs in Minecraft
2. Move the Heading Marker pack back to available
3. Click "Done"

Or delete it from the `resourcepacks` folder in your Minecraft directory.

## Need Help?

If you encounter issues not covered here:
- Check the main README.md for usage instructions
- Report bugs on the GitHub issues page
- Make sure all files are in the correct locations
- Verify file permissions allow Minecraft to read the files

Happy navigating with Heading Marker! 🧭
