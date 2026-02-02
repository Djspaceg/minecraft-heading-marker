# Heading Marker - Vanilla Client Support

## Overview

This project provides waypoint markers for Minecraft 1.21.11+ that work with **vanilla clients** (no mods required on the client side).

## Two Implementations

### 1. **Datapack (Recommended for Vanilla Clients)**

Located in `/headingmarker/` - This is a pure datapack implementation that:

- ✅ Works with **vanilla clients** (no Fabric or mods needed on client)
- ✅ Uses Minecraft 1.21+ native waypoint system
- ✅ Renders waypoints in the vanilla Locator Bar
- ✅ Server delivers waypoints over-the-wire to clients
- ✅ Supports all 5 colors: red, blue, green, yellow, purple

**Installation:** Copy the `headingmarker/` folder to your server's `datapacks/` directory.

**Commands:**
```
/function headingmarker:set_macro {x:1000,y:64,z:-500}
/function headingmarker:set_2d {x:1000,z:-500}
/function headingmarker:set_here
/function headingmarker:remove
/function headingmarker:help
```

### 2. **Fabric Mod (Optional, Server-Only)**

Located in `/src/` - This is an optional Fabric mod that provides enhanced commands:

- Provides `/hm` commands (shorter syntax)
- Server-side only (clients don't need it)
- Requires Fabric Loader on server
- Fabric API is optional (recommended but not required)

**Installation:** Place the mod JAR in the server's `mods/` folder (Fabric Loader required).

**Commands:**
```
/hm set <color>
/hm set <x> <z>
/hm set <x> <y> <z>
/hm remove <color>
/hm list
/hm showdistance yes|no
/hm help
```

## For Vanilla Clients

If you're running a server and want vanilla clients to connect without any mods:

1. **Use the datapack** (in `/headingmarker/`)
2. **Don't install the Fabric mod** on the server (or if you do, clients still don't need anything)
3. Clients will see waypoints in their vanilla Locator Bar automatically

## How It Works

The datapack uses Minecraft 1.21+'s native waypoint system:

1. Creates invisible armor stands at waypoint locations
2. Uses the `/waypoint modify` command to mark them as trackable waypoints
3. Vanilla clients receive waypoint data from the server automatically
4. Waypoints render in the Locator Bar (above XP bar) with distance indicators

No client-side mods or resource packs are required!

## Compatibility

- **Minecraft Version:** 1.21.11+
- **Client:** Vanilla Minecraft (no mods needed)
- **Server:** Vanilla or Fabric (Fabric mod is optional)
- **Pack Format:** 94 (datapack), 48 (old version)

## Migration from Mod to Datapack

If you were previously using the Fabric mod with custom client rendering:

1. Remove the mod from client `mods/` folders
2. Install the datapack on the server
3. Clients will automatically receive waypoints via vanilla system
4. No client-side changes needed!
