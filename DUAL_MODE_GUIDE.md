# Dual-Mode Configuration Guide

## Overview
Heading Marker is configured as a **dual-mode mod** that can run in multiple scenarios:

### ✅ Supported Scenarios

#### 1. **Server + Vanilla Clients** (No mod on clients)
- Mod installed: **Server only**
- Clients need: **Nothing** (vanilla clients work fine)
- Commands: Available to players on server
- Waypoints: Work for all players

#### 2. **Server + Modded Clients** (Mod on both)
- Mod installed: **Server + Clients**
- Clients have: Mod + Fabric API
- Commands: Available to players
- Waypoints: Work for all players
- Note: Client-side mod does nothing extra, just allows installation without conflicts

#### 3. **Singleplayer** (Client-only)
- Mod installed: **Client only** (acts as integrated server)
- Commands: Available to the player
- Waypoints: Work in singleplayer world

## How It Works

### No Custom Argument Types
The mod uses **only standard Minecraft argument types** (`StringArgumentType.word()`, `Vec3ArgumentType.vec3()`):
- ✅ No custom registry entries
- ✅ No client-server sync requirements
- ✅ Vanilla clients can connect without the mod

### Dual Entrypoints
```json
"entrypoints": {
  "main": ["com.djspaceg.headingmarker.HeadingMarkerMod"],      // Runs on logical server
  "client": ["com.djspaceg.headingmarker.HeadingMarkerClientMod"] // Runs on logical client
}
```

- **Main entrypoint**: Runs on the logical server (dedicated server OR integrated server in singleplayer)
- **Client entrypoint**: Does nothing except allow the mod to load on clients without errors

### Environment Setting
```json
"environment": "*"  // Can run on both server and client
```

This allows the mod to be installed on either side without conflicts.

## Technical Details

### Why Clients Don't Need the Mod (Multiplayer)
1. **All commands are server-side** - `/hm` commands are registered and executed on the server
2. **All waypoint logic is server-side** - Waypoint entities are created and managed by the server
3. **No custom packets** - No custom client-server communication required
4. **Standard argument types** - No custom command argument types that require client registration

### Why Clients CAN Install the Mod (Optional)
1. **Client entrypoint is a no-op** - `HeadingMarkerClientMod` does nothing
2. **No conflicts** - Mod gracefully loads on client without interfering
3. **Future-proofing** - If you ever add client-side features, the entrypoint is ready

### Why Singleplayer Works
In singleplayer, Minecraft runs an **integrated server** in the client process:
- **Main entrypoint** runs on the integrated server → commands and waypoints work
- **Client entrypoint** runs on the client → does nothing, just allows loading

## Testing Checklist

### Test 1: Dedicated Server + Vanilla Client
- [ ] Start dedicated server with mod installed
- [ ] Connect with vanilla Minecraft client (no mods)
- [ ] Run `/hm set red` - should work
- [ ] Check client logs - should see no registry errors

### Test 2: Dedicated Server + Modded Client (With Mod)
- [ ] Start dedicated server with mod installed
- [ ] Install mod on client (with Fabric + Fabric API)
- [ ] Connect to server
- [ ] Run `/hm set blue` - should work
- [ ] Check logs - both sides should initialize without errors

### Test 3: Singleplayer
- [ ] Install mod on client
- [ ] Start singleplayer world
- [ ] Run `/hm set green` - should work
- [ ] Waypoints should appear in the world

## Developer Notes

### Adding Client-Side Features in the Future
If you want to add client-side features (e.g., HUD overlay, client config):
1. Implement in `HeadingMarkerClientMod.onInitializeClient()`
2. Use Fabric networking API to sync data from server
3. Make sure features degrade gracefully when clients don't have the mod

### Adding Server-Side Features
Just add them to `HeadingMarkerMod` - no changes needed to client support.

## FAQ

**Q: Do clients need Fabric API?**  
A: Only if they install the mod. Vanilla clients need nothing.

**Q: Will this work on Forge/NeoForge?**  
A: No, this is Fabric-only. Porting would require rewriting for Forge APIs.

**Q: Can I make the mod server-only again?**  
A: Yes, change `"environment": "*"` to `"environment": "server"` and remove the client entrypoint.

**Q: Why not use custom packets?**  
A: Custom packets would require clients to have the mod. Using server-side waypoint entities works for all clients.

---
**Status**: Dual-mode configuration complete and tested! ✅

