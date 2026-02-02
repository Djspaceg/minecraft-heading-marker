# Migration Guide: Fabric Mod to Vanilla Client Support

## Overview

This guide explains the changes made to support vanilla Minecraft clients connecting to servers without requiring Fabric API or any client-side mods.

## What Changed?

### Before (v1.0.5)
- Mod required Fabric API on **both server and client**
- Custom client-side rendering using mixins
- Custom networking packets (WaypointSyncPayload)
- Clients had to install:
  - Fabric Loader
  - Fabric API
  - Heading Marker mod JAR

### After (v1.0.6+)
- **Datapack is the primary solution** - no mods needed!
- Mod is **server-only and optional** (provides `/hm` commands)
- Uses vanilla Minecraft 1.21.11+ waypoint system
- Clients need: **Nothing!** Just vanilla Minecraft

## For Server Administrators

### Option 1: Pure Datapack (Recommended for Vanilla Clients)

1. Copy `headingmarker/` folder to your world's `datapacks/` directory
2. Run `/reload`
3. Done! Vanilla clients can now use waypoints

**Commands:**
```
/function headingmarker:set_macro {x:1000,y:64,z:-500}
/function headingmarker:set_here
/function headingmarker:remove
/function headingmarker:help
```

### Option 2: Datapack + Fabric Mod (Enhanced Commands)

1. Install Fabric Loader on the **server only**
2. Copy `headingmarker/` folder to `datapacks/` directory
3. Copy mod JAR to `mods/` folder
4. Restart server

**Commands:**
```
/hm set <color>
/hm set <x> <y> <z>
/hm remove <color>
/hm list
/hm help
```

Vanilla clients still work!

## For Players

### If Your Server Uses the Datapack

**No action needed!** Your vanilla Minecraft client works as-is.

Waypoints will appear in your Locator Bar (above the XP bar) automatically.

### If You Previously Installed the Mod

You can now **uninstall** these from your client:
- Heading Marker mod JAR
- Fabric API (if only used for Heading Marker)
- Fabric Loader (if only used for Heading Marker)

Waypoints will still work - the server sends them to your vanilla client!

## Technical Details

### How It Works Now

1. **Datapack creates waypoint entities**: Invisible armor stands at waypoint locations
2. **Uses vanilla `/waypoint modify` command**: Marks entities as trackable waypoints
3. **Server sends waypoint data**: Vanilla Minecraft protocol handles this automatically
4. **Client renders in Locator Bar**: Native Minecraft feature (1.21.11+)

### What Was Removed

From the mod:
- `HeadingMarkerClient.java` - Client entrypoint
- `ExperienceBarMixin.java` - Custom HUD rendering
- `headingmarker.mixins.json` - Mixin configuration
- `WaypointSyncPayload` - Custom networking packets
- `ShowDistanceSyncPayload` - Custom networking packets
- All Fabric API networking code

From `fabric.mod.json`:
- `"client"` entrypoint
- `"mixins"` reference
- Fabric API moved from "depends" to "recommends"
- Environment changed from `"*"` to `"server"`

### What Remains in the Mod

The mod now only provides:
- `/hm` command registration (server-side only)
- Server-side waypoint storage and persistence
- Command argument types (ColorArgumentType)

The mod does NOT:
- Render waypoints (datapack handles this)
- Send custom packets (vanilla system handles this)
- Require client installation (server-only)
- Require Fabric API (optional)

## Troubleshooting

### "Unknown function headingmarker:..." error

Make sure the datapack is installed in your world's `datapacks/` folder, not the mod.

### Waypoints don't appear

1. Check you're running Minecraft 1.21.11 or later
2. Verify the datapack loaded: `/datapack list`
3. Run `/reload` to reload datapacks
4. Try setting a waypoint: `/function headingmarker:set_here`

### Can I still use `/hm` commands?

Yes, but you need to install the Fabric mod on the **server only**. Clients don't need it.

### Do I need both the datapack and the mod?

- **For waypoint rendering**: You need the datapack
- **For `/hm` commands**: You need the mod (server-only)
- **Recommended**: Install both on server, clients need nothing

## Version Compatibility

- **Minecraft:** 1.21.11 or later (required for vanilla waypoint system)
- **Datapack Format:** 94 (pack_format in pack.mcmeta)
- **Mod (Optional):** Requires Fabric Loader 0.16.5+, Fabric API recommended but optional

## Migration Timeline

1. **Before Feb 2026:** Mod required Fabric API on client
2. **Feb 2 2026:** Mod made server-only, Fabric API optional
3. **Future:** Mod may be deprecated entirely in favor of datapack

## Questions?

See:
- `README.md` - Main project documentation
- `README_VANILLA.md` - Vanilla client support details
- `README_MOD.md` - Mod-specific documentation (server-only)
- `INSTALLATION.md` - Installation instructions
