# Copilot / AI Agent Instructions for Heading Marker

## Target Environment

**Minecraft 26.1 / Fabric / Kotlin — Java 25**

### Rules:

1. **NO downgrade suggestions** - 26.1 is current, not going backwards
2. **SEARCH CODEBASE FIRST** - Don't guess APIs, look at working code
3. **TEST COMPILATION** - Use `get_errors` tool before claiming success
4. **FIX YOUR MISTAKES** - If you reference deprecated code, YOU fix it

### API Quick Reference (MC 26.1 / Mojang mappings):

```kotlin
// Player access:
player.level()              // ServerLevel
player.level().server       // MinecraftServer
player.uuid                 // UUID
player.gameProfile.name     // Stable profile name (use for selectors)
player.name.string          // Display name (may differ from profile name)

// Dimension:
HeadingMarkerMod.getDimensionKey(world.dimension())  // "overworld", "the_nether", "the_end"

// Permission check (hasPermission removed in 26.1):
(player.level() as ServerLevel).server.playerList.isOp(
    NameAndId(player.uuid, player.name.string)
)
```

### Before ANY code change:

```bash
# After changes, verify:
./gradlew build
```

---

## Project Overview

Fabric mod (Kotlin) + data pack for per-player, per-dimension waypoint markers in Minecraft 26.1.

**Key Structure:**

- `src/main/java/com/daolan/headingmarker/` - Mod code (Kotlin .kt files)
- `datapack_for_headingmarker/headingmarker_datapack/` - Data pack functions
- 5 colors × 3 dimensions × per-player = 15 total waypoints per player

**Critical Invariant:** Waypoints are isolated by BOTH player UUID AND dimension.

## Common Issues & Fixes

### Issue: Compilation errors about missing methods

**Cause:** Using older MC APIs that don't exist in 26.1
**Fix:** Search the codebase for working examples first

### Issue: Optional serialization crashes

**Cause:** Gson can't serialize `Optional<T>` due to Java module system
**Fix:** Use `GsonBuilder` with custom `TypeAdapter` (see `WaypointStorage.kt`)

### Issue: Waypoints appearing in wrong dimensions

**Cause:** Missing dimension isolation in data structure
**Fix:** Map structure must be `Player → Dimension → Color → Data`

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

- `HeadingMarkerMod.kt` - Player/world/dimension access
- `WaypointStorage.kt` - Gson with Optional handling
- `HeadingMarkerCommands.kt` - Command registration

---

**Bottom Line:** Search existing code → Use what works → Test it compiles → Done.
