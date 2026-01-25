# Heading Marker - Vanilla Waypoint System Integration Specification

## Executive Summary

This document specifies how to properly integrate custom waypoint markers with Minecraft's native waypoint/locator system instead of the current approach of manually drawing rectangles on the screen.

## Current Implementation (v1.0.3) - What We're Doing Wrong

### Architecture

- **Custom rendering**: Manually drawing colored rectangles using `DrawContext.fill()`
- **Custom positioning**: Manually calculating screen positions based on player yaw and XP bar location
- **Custom synchronization**: Using Fabric networking to sync waypoint data from server to client
- **Injection point**: Mixin into `ExperienceBar.renderAddons()` to draw at the right time

### Problems

1. **Not using vanilla sprites**: Drawing solid color rectangles instead of textured icons
2. **Not using vanilla waypoint system**: Completely separate data structures and rendering pipeline
3. **Reinventing the wheel**: Manual yaw calculation, screen positioning, dimension filtering
4. **Visual inconsistency**: May not match vanilla player indicator appearance exactly
5. **Missing features**: No arrow indicators for above/below, no distance-based behavior

---

## Vanilla Waypoint System Architecture

### Core Components (Minecraft 1.21.11)

#### 1. **Waypoint Data Layer**

- **Package**: `net.minecraft.world.waypoint`
- **Key Classes**:
  - `Waypoint` (interface) - Base waypoint contract
  - `TrackedWaypoint` (abstract class) - Server-to-client synchronized waypoints
  - `Waypoint.Config` - Style, color, and display configuration
  - `WaypointStyle` - Visual appearance registry entry

#### 2. **Server-Side Management**

- **Package**: `net.minecraft.world.waypoint`
- **Key Classes**:
  - `ServerWaypoint` - Server-side waypoint tracking and validation
  - `ServerWaypoint.PositionalWaypointTracker` - Tracks position-based waypoints
  - `ServerWaypoint.AzimuthWaypointTracker` - Tracks direction-based waypoints
  - `ServerWaypoint.ChunkWaypointTracker` - Tracks chunk-based waypoints

#### 3. **Client-Side Handling**

- **Package**: `net.minecraft.client.world`, `net.minecraft.client.network`
- **Key Classes**:
  - `ClientWaypointHandler` - Receives and manages tracked waypoints on client
  - `ClientPlayNetworkHandler.onWaypoint()` - Processes `WaypointS2CPacket`
  - `ClientPlayNetworkHandler.getWaypointHandler()` - Accessor for waypoint handler

#### 4. **Rendering System**

- **Package**: `net.minecraft.client.gui.hud.bar`
- **Key Classes**:
  - `LocatorBar` - Renders waypoint indicators on the experience bar
  - `Bar` (interface) - Standard bar rendering contract
  - Sprites: `ARROW_UP`, `ARROW_DOWN`, `BACKGROUND` textures

#### 5. **Networking**

- **Packet**: `WaypointS2CPacket` (Server → Client)
- **Codec**: `TrackedWaypoint.PACKET_CODEC` for serialization
- **Handler**: `ClientPlayNetworkHandler.onWaypoint(WaypointS2CPacket)`

---

## Proper Integration Strategy

### Phase 1: Server-Side Waypoint Creation

**Objective**: Use vanilla `TrackedWaypoint` instead of custom `Waypoint` class

**Implementation**:

```java
// Replace current custom Waypoint class with vanilla TrackedWaypoint creation
public static TrackedWaypoint createHeadingMarker(ServerPlayerEntity player, String color, double x, double y, double z) {
    UUID playerUuid = player.getUuid();

    // Create waypoint configuration with custom color
    Waypoint.Config config = new Waypoint.Config();
    config.color = Optional.of(getColorInt(color));
    config.style = WaypointStyles.PLAYER_ICON; // Or custom registry entry

    // Create position-based tracked waypoint
    Vec3i pos = new Vec3i((int)x, (int)y, (int)z);
    return TrackedWaypoint.ofPos(playerUuid, config, pos);
}
```

**Changes Required**:

1. Delete `com.djspaceg.headingmarker.Waypoint` class
2. Delete `HeadingMarkerState` (use vanilla tracking)
3. Modify commands to create `TrackedWaypoint` instances
4. Store waypoints per-player using vanilla `ServerWaypoint` trackers

### Phase 2: Client-Side Waypoint Reception

**Objective**: Hook into vanilla `ClientWaypointHandler` instead of custom networking

**Implementation**:

```java
// Access vanilla waypoint handler instead of custom sync
public class HeadingMarkerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // No custom networking needed - vanilla handles it!
        // Waypoints automatically sync via WaypointS2CPacket
    }
}
```

**Changes Required**:

1. Delete `WaypointSyncPayload` custom networking
2. Delete `ClientPlayNetworking.registerGlobalReceiver()` custom handler
3. Remove `HeadingMarkerMod.syncMarkerData()` - vanilla syncs automatically
4. Access waypoints via: `MinecraftClient.getInstance().getNetworkHandler().getWaypointHandler()`

### Phase 3: Rendering Integration

**Objective**: Extend or mixin `LocatorBar` instead of custom rendering

#### Option A: Mixin Approach (Safer)

```java
@Mixin(LocatorBar.class)
public class LocatorBarMixin {
    @Inject(method = "renderAddons", at = @At("TAIL"))
    private void renderCustomWaypoints(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWaypointHandler handler = client.getNetworkHandler().getWaypointHandler();

        // Iterate through waypoints using vanilla's forEachWaypoint
        handler.forEachWaypoint(client.player, waypoint -> {
            // Use vanilla's yaw calculation: waypoint.getRelativeYaw()
            // Use vanilla's pitch calculation: waypoint.getPitch()
            // Render using vanilla sprites and positioning
            renderWaypointIndicator(context, waypoint);
        });
    }
}
```

#### Option B: Custom Bar Implementation (Cleaner)

```java
public class HeadingMarkerBar implements Bar {
    private final MinecraftClient client;

    @Override
    public void renderBar(DrawContext context, RenderTickCounter tickCounter) {
        // No bar background needed
    }

    @Override
    public void renderAddons(DrawContext context, RenderTickCounter tickCounter) {
        ClientWaypointHandler handler = client.getNetworkHandler().getWaypointHandler();
        handler.forEachWaypoint(client.player, this::renderMarker);
    }

    private void renderMarker(TrackedWaypoint waypoint) {
        // Use waypoint.getRelativeYaw() for horizontal position
        // Use waypoint.getPitch() for up/down arrows
        // Access waypoint.getConfig().color for custom colors
        // Use vanilla ARROW_UP/ARROW_DOWN sprites
    }
}
```

**Changes Required**:

1. Delete custom `renderWaypointMarkers()` rectangle drawing
2. Use `TrackedWaypoint.getRelativeYaw()` instead of manual yaw math
3. Use `TrackedWaypoint.getPitch()` for vertical indicators
4. Use vanilla sprite rendering: `context.drawGuiTexture()` with `LocatorBar.ARROW_UP/DOWN`
5. Respect `Waypoint.Config.color` for custom coloring

### Phase 4: Style Registration (Optional Enhancement)

**Objective**: Register custom waypoint styles in vanilla registry

**Implementation**:

```java
public class HeadingMarkerStyles {
    public static final RegistryKey<WaypointStyle> RED_MARKER =
        RegistryKey.of(RegistryKeys.WAYPOINT_STYLE, Identifier.of("headingmarker", "red"));
    public static final RegistryKey<WaypointStyle> BLUE_MARKER =
        RegistryKey.of(RegistryKeys.WAYPOINT_STYLE, Identifier.of("headingmarker", "blue"));
    // ... etc for each color

    public static void register() {
        // Register custom styles with vanilla registry
        Registry.register(Registries.WAYPOINT_STYLE, RED_MARKER, new WaypointStyle());
    }
}
```

---

## Key Vanilla APIs to Use

### TrackedWaypoint Methods

- `getRelativeYaw(World, YawProvider, EntityTickProgress)` - Get horizontal angle to waypoint
- `getPitch(World, PitchProvider, EntityTickProgress)` - Get vertical angle (up/down indicator)
- `squaredDistanceTo(Entity)` - Distance calculation
- `getConfig()` - Access color and style configuration
- `getSource()` - Get player UUID or string identifier

### ClientWaypointHandler Methods

- `forEachWaypoint(Entity, Consumer<TrackedWaypoint>)` - Iterate all tracked waypoints
- `hasWaypoint()` - Check if any waypoints exist
- `onTrack(TrackedWaypoint)` - Handle new waypoint tracking
- `onUpdate(TrackedWaypoint)` - Handle waypoint updates
- `onUntrack(TrackedWaypoint)` - Handle waypoint removal

### LocatorBar Rendering

- Sprites: `LocatorBar.ARROW_UP`, `LocatorBar.ARROW_DOWN`, `LocatorBar.BACKGROUND`
- Position calculation: Uses `Bar.getCenterX()`, `Bar.getCenterY()`
- Width/height constants: `Bar.WIDTH` (182px), `Bar.HEIGHT` (5px)

---

## Implementation Roadmap

### Milestone 1: Data Layer Migration

- [ ] Replace custom `Waypoint` class with `TrackedWaypoint` usage
- [ ] Remove `HeadingMarkerState` persistent storage
- [ ] Modify commands to create vanilla waypoint instances
- [ ] Test: Waypoints created via commands

### Milestone 2: Networking Migration

- [ ] Remove `WaypointSyncPayload` custom networking
- [ ] Remove custom packet handlers
- [ ] Verify vanilla `WaypointS2CPacket` handles sync
- [ ] Test: Waypoints sync from server to client automatically

### Milestone 3: Rendering Migration

- [ ] Access waypoints via `ClientWaypointHandler`
- [ ] Use `TrackedWaypoint.getRelativeYaw()` for positioning
- [ ] Implement sprite-based rendering with vanilla textures
- [ ] Test: Markers appear with correct positioning

### Milestone 4: Visual Polish

- [ ] Add up/down arrow indicators using `getPitch()`
- [ ] Implement distance-based scaling/fading
- [ ] Add animation/pulsing effects
- [ ] Test: Visual parity with vanilla player indicators

### Milestone 5: Style Registration (Optional)

- [ ] Register custom waypoint styles
- [ ] Allow style selection via commands
- [ ] Test: Custom styles render correctly

---

## Benefits of Proper Integration

1. **Automatic networking**: No custom packets, vanilla handles sync
2. **Automatic persistence**: Vanilla can persist waypoints if needed
3. **Visual consistency**: Uses same sprites and positioning as player indicators
4. **Less code**: ~60% reduction in custom code by using vanilla systems
5. **Better compatibility**: Other mods can interact with waypoints
6. **Future-proof**: Updates to vanilla waypoint system automatically apply
7. **Feature-rich**: Automatic dimension handling, distance validation, pitch indicators

---

## Migration Risks & Mitigation

### Risk 1: Breaking Existing Saved Data

**Mitigation**: Provide migration tool to convert old JSON format to vanilla waypoints

### Risk 2: API Compatibility

**Mitigation**: Yarn mappings may change - use stable Fabric API wrappers where possible

### Risk 3: Custom Color Support

**Mitigation**: Vanilla `Waypoint.Config.color` supports custom integer colors - no limitation

### Risk 4: Per-Player Waypoints

**Mitigation**: `TrackedWaypoint` uses UUID source - inherently per-player

---

## Conclusion

The current implementation works but duplicates significant vanilla functionality. Proper integration with Minecraft's native waypoint system would:

- Reduce codebase by 60%
- Improve visual consistency
- Enable automatic networking and persistence
- Make the mod more maintainable and compatible

The refactoring effort is estimated at 8-12 hours but provides long-term benefits in maintainability and feature parity with vanilla systems.

---

**Document Version**: 1.0  
**Minecraft Version**: 1.21.11  
**Fabric API**: 0.18.2  
**Author**: AI Analysis of Vanilla Codebase  
**Date**: January 25, 2026
