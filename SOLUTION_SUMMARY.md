# Solution Summary: Vanilla Client Support via Armor Stand Waypoints

## Problem

The Fabric mod required Fabric API on both server and client, preventing vanilla clients from
connecting.

## Solution Implemented

The mod uses Minecraft's native waypoint system (the Locator Bar) by creating armor stand entities
with the `waypoint_transmission_range` attribute.

## How It Works

### Server-Side (Mod)

1. When `/hm set` is called, the mod creates an invisible armor stand at the waypoint
   location
2. Sets these properties on the armor stand:
    - Invisible, Invulnerable, NoGravity, Silent, Marker
    - Custom name `hm:<key>`, where `<key>` is the waypoint's 8-character marker key
    - `waypoint_transmission_range` attribute = 9999

3. The armor stand entity is spawned in the world
4. Waypoint data is stored for persistence

### Vanilla Protocol (Automatic)

1. Minecraft automatically detects entities with `waypoint_transmission_range` attribute
2. Sends waypoint data to nearby clients via vanilla `WaypointS2CPacket`
3. No custom networking or packets needed!

### Client-Side (Vanilla Minecraft)

1. Vanilla client receives waypoint data through standard Minecraft protocol
2. Renders waypoint in the Locator Bar (above XP bar)
3. Shows distance and direction automatically
4. **No mods, Fabric Loader, or Fabric API required on client!**

## Code Changes

### What Was Removed

- ❌ `HeadingMarkerClient.java` - Client entrypoint
- ❌ `ExperienceBarMixin.java` - Custom rendering mixin
- ❌ `headingmarker.mixins.json` - Mixin configuration
- ❌ `WaypointSyncPayload` - Custom networking
- ❌ `ShowDistanceSyncPayload` - Custom networking
- ❌ All custom client-server networking code
- ❌ `/hm showdistance` command (non-functional)
- ❌ Old `playerShowDistance` HashMap and methods

### What Was Added

- ✅ Armor stand entity creation with waypoint attributes
- ✅ `recreateWaypointEntities()` for player join
- ✅ Entity ID tracking for cleanup
- ✅ Vanilla waypoint attribute registration
- ✅ Server tick handler for distance updates
- ✅ Always-on actionbar distance display with colored text

### What Remains

- ✅ Server-side command system (`/hm` commands)
- ✅ Server-side waypoint storage and persistence
- ✅ `environment: "*"` in fabric.mod.json (loads on server, optional on client)

## Key Features

**For Server Admins:**

- Install mod on Fabric server only
- Vanilla clients can connect without any mods
- `/hm` commands work for all players; only `/hm purge` needs operator

**For Players:**

- Connect with vanilla Minecraft 26.3
- See waypoints in Locator Bar automatically
- See distances on the actionbar automatically, e.g. `🔴 Red 245  🔵 Home 180`
- No client-side installation required

## Distance Display Feature

Distances to every waypoint in the player's current dimension are always shown on the actionbar.
There is nothing to toggle.

- Format: `<emoji> <label> <distance>`, e.g. `🔴 Red 245  🔵 Home 180  🟢 Green 12`
- The label is the waypoint's name (first 12 characters), or its color if unnamed
- The distance is the 3D distance in whole blocks, colored to match the waypoint
- Updates every 5 ticks, and is only re-sent when the text changes
- Server-side only, so it works for vanilla clients

## Technical Details

### Minecraft Waypoint System

Minecraft 1.21.6 introduced built-in waypoint support (the Locator Bar):

- Entities with `waypoint_transmission_range` attribute are tracked
- Server automatically sends waypoint data to clients
- Clients render waypoints in the Locator Bar
- Vanilla `/waypoint` commands can modify waypoint properties

### Why Armor Stands?

- Invisible and non-interactive
- Support custom attributes
- Persist in the world
- Can be easily cleaned up

### Entity Lifecycle

1. **Creation**: Mod spawns armor stand when waypoint is set
2. **Transmission**: Minecraft sends to nearby clients automatically
3. **Rendering**: Vanilla client displays in Locator Bar
4. **Removal**: Mod discards armor stand when waypoint is removed
5. **Persistence**: Coordinates saved to JSON, entities recreated on join

## Testing Recommendations

### Test 1: Vanilla Client Connection

1. Install mod on Fabric server
2. Connect with vanilla Minecraft 26.3 client
3. Run `/hm set red`
4. ✅ Expected: Waypoint appears in client's Locator Bar

### Test 2: Multiple Waypoints

1. Set multiple waypoints with different colors
2. ✅ Expected: All waypoints visible in Locator Bar

### Test 3: Persistence

1. Set waypoints
2. Disconnect and reconnect
3. ✅ Expected: Waypoints restored automatically

### Test 4: Removal

1. Set waypoint
2. Run `/hm remove <selector>` (a marker key, color, or name)
3. ✅ Expected: Matching waypoints disappear from Locator Bar

### Test 5: Distance Display

1. Set a waypoint
2. ✅ Expected: Actionbar shows its distance (e.g., `🔴 Red 245`)
3. Walk towards/away from it
4. ✅ Expected: Distance updates on the actionbar
5. Remove all waypoints in the dimension
6. ✅ Expected: Actionbar cleared

### Test 6: No OP Required

1. Test as non-OP player
2. Run `/hm set`, `/hm list`, and `/hm remove <selector>`
3. ✅ Expected: All work without operator permissions; `/hm purge` is not offered

## Advantages Over Previous Approach

**Before (Custom Networking):**

- Required Fabric API on client ❌
- Required custom client mod ❌
- Custom rendering code ❌
- Custom packet handling ❌
- Maintenance burden ❌

**After (Vanilla Waypoints):**

- No client requirements ✅
- Uses vanilla Minecraft protocol ✅
- Automatic rendering ✅
- No custom networking ✅
- Future-proof ✅

## Compatibility

- **Minecraft Version**: 26.3
- **Server**: Fabric server with Fabric API
- **Client**: Vanilla Minecraft (no mods needed)
- **Datapack**: The `datapack_for_headingmarker` remains a separate, independent implementation

## Conclusion

The mod now works with vanilla clients by leveraging Minecraft's built-in waypoint system. No custom
client-side code, no Fabric API on client, no manual installation required. The solution is clean,
maintainable, and future-proof.
