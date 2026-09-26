# Copilot / AI Agent Instructions for Heading Marker

## Target Environment

**Minecraft 26.3 / Fabric / Kotlin — Java 25**

### Rules:

1. **NO downgrade suggestions** - 26.3 is current, not going backwards
2. **SEARCH CODEBASE FIRST** - Don't guess APIs, look at working code
3. **TEST COMPILATION** - Use `get_errors` tool before claiming success
4. **FIX YOUR MISTAKES** - If you reference deprecated code, YOU fix it

### API Quick Reference (MC 26.3 / Mojang mappings):

```kotlin
// Player access:
player.level()              // ServerLevel
player.level().server       // MinecraftServer
player.uuid                 // UUID
player.gameProfile.name     // Stable profile name (use for selectors)
player.name.string          // Display name (may differ from profile name)

// Dimension ids (bare for minecraft:*, namespaced otherwise, e.g. "mymod:mining"):
Dimensions.idOf(level)            // "overworld", "the_nether", "the_end"
Dimensions.levelFor(server, id)   // any loaded level, not just the vanilla three

// Permission check (CommandSourceStack.hasPermission was removed in 26.1):
Commands.hasPermission<CommandSourceStack>(Commands.LEVEL_GAMEMASTERS)  // a Predicate

// Entity invulnerability (setInvulnerable renamed in 26.3):
entity.setPermanentlyInvulnerable(true)
```

### Before ANY code change:

```bash
# After changes, verify:
./gradlew build
```

---

## Project Overview

Fabric mod (Kotlin) + data pack for per-player, per-dimension waypoint markers in Minecraft 26.3.

**Key Structure:**

- `src/main/java/com/daolan/headingmarker/` - Mod code (Kotlin .kt files)
- `datapack_for_headingmarker/headingmarker_datapack/` - Data pack functions
- Waypoints are keyed by an 8-character marker key; there's no per-color or per-dimension limit

**Critical Invariant:** Waypoints are isolated by BOTH player UUID AND dimension.

## Common Issues & Fixes

### Issue: Compilation errors about missing methods

**Cause:** Using older MC APIs that don't exist in 26.3
**Fix:** Search the codebase for working examples first

### Issue: Optional serialization crashes

**Cause:** Gson reflecting over runtime or Minecraft types
**Fix:** Serialize plain DTOs (`PlayerFile` / `StoredWaypoint` in `WaypointStorage.kt`), never
the `Waypoint` model directly

### Issue: Marker entity still on the locator bar after removal, or missing far away

**Cause:** Looking marker armor stands up with `Level.getEntity`. It can't see entities in
unloaded chunks, but vanilla still transmits those.
**Fix:** Keep going through `MarkerEntities`, which holds each stand by reference. Verify
entity changes with a gametest (`src/gametest/`); `ProbeGameTests` logs how the vanilla waypoint
manager treats markers.

### Issue: Waypoints appearing in wrong dimensions

**Cause:** Missing dimension isolation in data structure
**Fix:** Map structure must be `Player → Dimension → MarkerKey → Data`

## Quick Commands

```bash
# Build
./gradlew build

# Compile only
./gradlew compileKotlin

# Test in-game
./gradlew runServer
```

## Working Code Reference

See these files for correct API usage:

- `HeadingMarkerMod.kt` - Fabric event wiring; `HeadingMarkerMod.service()` is the running server's `WaypointService`
- `WaypointService.kt` - Operations that keep data, marker entities, HUD, and saves in step
- `WaypointRegistry.kt` - Pure in-memory data (owner → dimension → key), selectors, dirty tracking
- `entity/MarkerEntities.kt` - Armor stand markers that feed the vanilla locator bar
- `storage/WaypointStorage.kt` - JSON load/save and format migration
- `HeadingMarkerCommands.kt` - Command tree and chat feedback only
- `src/gametest/` - In-game tests (`./gradlew runGameTest`, also part of `build`)

---

**Bottom Line:** Search existing code → Use what works → Test it compiles → Done.
