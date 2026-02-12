# Copilot / AI Agent Instructions for Heading Marker 🔧

## ⚠️ CRITICAL - READ FIRST ⚠️

**Minecraft 1.21.11 (Current Release - February 2026)**

### Non-Negotiable Rules:
1. **NO downgrade suggestions** - 1.21.11 is current, not going backwards
2. **SEARCH CODEBASE FIRST** - Don't guess APIs, look at working code
3. **TEST COMPILATION** - Use `get_errors` tool before claiming success
4. **FIX YOUR MISTAKES** - If you reference deprecated code, YOU fix it

### API Quick Reference (1.21.11):
```java
// ❌ WRONG (doesn't exist):
player.getWorld()
player.getServer()

// ✅ CORRECT:
player.getEntityWorld()
((ServerWorld) player.getEntityWorld()).getServer()
```

### Before ANY code change:
```bash
# Search for existing usage:
grep -r "methodName" src/

# After changes, verify:
get_errors on modified files
```

---

## Project Overview

Fabric mod + data pack for per-player, per-dimension waypoint markers in Minecraft 1.21.11.

**Key Structure:**
- `src/main/java/com/djspaceg/headingmarker/` - Mod code
- `datapack_for_headingmarker/headingmarker_datapack/` - Data pack functions
- 5 colors × 3 dimensions × per-player = 15 total waypoints per player

**Critical Invariant:** Waypoints are isolated by BOTH player UUID AND dimension.

## Common Issues & Fixes

### Issue: Compilation errors about missing methods
**Cause:** Using 1.20.x APIs that don't exist in 1.21.11  
**Fix:** Search the codebase for working examples first

### Issue: Optional serialization crashes
**Cause:** Gson can't serialize `Optional<T>` due to Java module system  
**Fix:** Use `GsonBuilder` with custom `TypeAdapter` (see `WaypointStorage.java`)

### Issue: Waypoints appearing in wrong dimensions
**Cause:** Missing dimension isolation in data structure  
**Fix:** Map structure must be `Player → Dimension → Color → Data`

## Quick Commands

```bash
# Build
gradlew build

# Compile only
gradlew compileJava

# Test in-game
gradlew runClient
```

## Working Code Reference

See these files for correct API usage:
- `HeadingMarkerMod.java` - Player/world/dimension access
- `WaypointStorage.java` - Gson with Optional handling
- `HeadingMarkerCommands.java` - Command registration

---

**Bottom Line:** Search existing code → Use what works → Test it compiles → Done.

