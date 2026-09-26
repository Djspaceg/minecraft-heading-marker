# Quick Reference: Dual-Mode Mod Setup

## ✅ COMPLETE - Your Mod Configuration

### What Works Now

| Scenario                          | Server Has Mod | Client Has Mod | Result                                            |
|-----------------------------------|----------------|----------------|---------------------------------------------------|
| **Multiplayer (Vanilla Clients)** | ✅ Yes          | ❌ No           | ✅ Works! Clients can connect without the mod      |
| **Multiplayer (Modded Clients)**  | ✅ Yes          | ✅ Yes          | ✅ Works! No conflicts, mod loads on both sides    |
| **Singleplayer**                  | N/A            | ✅ Yes          | ✅ Works! Integrated server has full functionality |

### Key Configuration

#### fabric.mod.json

```json
{
  "environment": "*",  // ← Runs on server OR client
  "entrypoints": {
    "main": ["HeadingMarkerMod"],      // ← Server-side logic
    "client": ["HeadingMarkerClientMod"] // ← Passive client entrypoint
  }
}
```

#### HeadingMarkerCommands.kt

- ✅ No custom argument type registration
- ✅ Uses StringArgumentType and DoubleArgumentType instead
- ✅ All commands server-side
- ✅ All waypoint logic server-side

#### HeadingMarkerClientMod.kt

- ✅ No-op implementation
- ✅ Allows mod to load on clients
- ✅ Ready for future client features

### Why It Works

1. **No custom argument types** → Vanilla clients don't need registry sync
2. **Passive client entrypoint** → Mod can be installed on clients without conflicts
3. **Server-side commands** → All logic runs on logical server (dedicated or integrated)
4. **Standard Brigadier types** → `StringArgumentType`, `DoubleArgumentType` work everywhere

### Build & Deploy

```bash
# Build the mod
./gradlew build

# Find the JAR
build/libs/minecraft-heading-marker-<version>.jar

# Deploy to server
Copy to: server/mods/

# Optional: Install on clients for singleplayer
Copy to: .minecraft/mods/
```

### Testing Commands

```
/hm help                  # Show help
/hm set red               # Set red waypoint at current position
/hm set blue 100 64 200   # Set blue waypoint at x y z
/hm set 100 200           # Set waypoint at x z (your Y), least-used color
/hm list                  # List waypoints and keys in this dimension
/hm rename red Home       # Name every red waypoint in this dimension "Home"
/hm remove Home           # Remove waypoints by key, color, or name
```

### Troubleshooting

**Problem**: Vanilla clients still can't connect

- ✅ **Fixed**: Removed custom argument types (ColorArgumentType)
- ✅ **Using**: Standard StringArgumentType instead

**Problem**: Mod won't load on singleplayer client

- ✅ **Fixed**: Added client entrypoint (HeadingMarkerClientMod)
- ✅ **Set**: environment = "*" to allow client loading

**Problem**: Clients get registry sync errors

- ✅ **Fixed**: No custom registries used
- ✅ **Using**: Only standard Minecraft command argument types

---

## Summary

✅ **Dual-mode**: Server OR client  
✅ **Optional on clients**: Vanilla clients can connect to servers  
✅ **Singleplayer support**: Works in singleplayer with integrated server  
✅ **No conflicts**: Can be installed on both sides safely  
✅ **No compilation errors**: Ready to build and deploy

**Status**: COMPLETE and TESTED! 🎉

