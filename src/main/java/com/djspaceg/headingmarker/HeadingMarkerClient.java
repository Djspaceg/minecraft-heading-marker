package com.djspaceg.headingmarker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import com.djspaceg.headingmarker.waypoint.EntityTickProgress;
import com.djspaceg.headingmarker.waypoint.TrackedWaypoint;
import com.djspaceg.headingmarker.waypoint.Waypoint;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Helper class to store both waypoint and position
record ClientMarker(TrackedWaypoint waypoint, Vec3i pos) {
}

public class HeadingMarkerClient implements ClientModInitializer {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("headingmarker-client");

    // Client-side waypoint storage: color -> ClientMarker
    private static final Map<String, ClientMarker> clientWaypoints = new HashMap<>();

    /**
     * Called by ExperienceBarMixin to render waypoint markers.
     * Uses vanilla ClientWaypointHandler and TrackedWaypoint.getRelativeYaw() for proper integration.
     */
    public static void renderWaypointMarkers(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Early exit if no waypoints
        if (clientWaypoints.isEmpty()) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int cx = screenWidth / 2;
        // Vertically center marker on the XP bar (vanilla bar is at y = screenHeight - 32, bar height is 5px)
        int indicatorY = screenHeight - 32 + 5; // Move down 5px to center on bar
        // XP bar width is 182 pixels (vanilla size)
        int barHalfWidth = 91; // 182 / 2
        float maxAngle = 90.0f; // Show markers within 90 degrees left/right

        // Create EntityTickProgress implementation for waypoint calculations
        EntityTickProgress tickProgress = new EntityTickProgress() {
            @Override
            public float getTickProgress(net.minecraft.entity.Entity entity) {
                // Return 1.0f for smooth rendering - tickCounter field access varies by Yarn version
                return 1.0f;
            }
        };

        // Create YawProvider for camera yaw
        TrackedWaypoint.YawProvider yawProvider = new TrackedWaypoint.YawProvider() {
            @Override
            public float getCameraYaw() {
                return client.player.getYaw();
            }

            @Override
            public Vec3d getCameraPos() {
                return client.player.getEyePos();
            }
        };

        // Show distance toggle (per player)
        boolean showDistance = HeadingMarkerMod.getShowDistance(client.player.getUuid());

        // Iterate over our client-side waypoints
        for (ClientMarker marker : clientWaypoints.values()) {
            TrackedWaypoint waypoint = marker.waypoint();
            Vec3i pos = marker.pos();
            // Use vanilla's yaw calculation - this handles all the math for us!
            double relativeYaw = waypoint.getRelativeYaw(client.world, yawProvider, tickProgress);

            // Calculate distance to marker (horizontal only)
            double dx = pos.getX() + 0.5 - client.player.getX();
            double dz = pos.getZ() + 0.5 - client.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            // Scale in discrete steps at the expected distances: 128, 384, 1024
            float scale;
            if (dist <= 128) {
                scale = 1.0f;          // full size
            } else if (dist <= 384) {
                scale = 0.75f;         // 75% size
            } else if (dist <= 1024) {
                scale = 0.5f;          // 50% size
            } else {
                scale = 0.25f;         // smallest for extreme distances
            }
            // Keep a sensible minimum marker pixel size so steps are visible
            // Marker base is 8px at full scale; reduce minimum enforced size to 2px to allow small steps

            // Convert yaw to screen position
            // relativeYaw is in degrees, where 0 = straight ahead, -90 = left, +90 = right
            if (Math.abs(relativeYaw) <= maxAngle) {
                float pixelsPerDegree = barHalfWidth / maxAngle;
                float xOffset = (float) relativeYaw * pixelsPerDegree;
                // Marker size (square): 7px at full scale
                int markerSize = Math.round(7 * scale);
                if (markerSize < 2) markerSize = 2; // allow smaller minima so steps are visible
                int renderX = (int) (cx + xOffset) - markerSize / 2;
                int renderY = indicatorY - markerSize / 2;

                // Get custom color from waypoint config
                int colorInt = waypoint.getConfig().color.orElse(0xFFFFFF);

                // Draw marker as a square
                context.fill(renderX, renderY, renderX + markerSize, renderY + markerSize, colorInt | 0xFF000000);
                // Add slight shadow/outline for visibility
                context.fill(renderX - 1, renderY - 1, renderX + markerSize + 1, renderY, 0xFF000000);
                context.fill(renderX - 1, renderY + markerSize, renderX + markerSize + 1, renderY + markerSize + 1, 0xFF000000);

                // Draw distance above marker if enabled
                if (showDistance) {
                    String distStr = String.format("%.0f m", dist);
                    int textColor = colorInt | 0xFF000000;
                    int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(distStr);
                    int textX = renderX + markerSize / 2 - textWidth / 2;
                    int textY = renderY - 18; // Move distance text higher (was -12), now -18 to avoid health bar
                    // Draw colored text with shadow
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, distStr, textX, textY, textColor);
                }
            }
        }
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Heading Marker Client (Custom Networking + Vanilla Waypoint Math)...");

        // Register networking receiver
        ClientPlayNetworking.registerGlobalReceiver(HeadingMarkerMod.WaypointSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.remove()) {
                    clientWaypoints.remove(payload.color());
                    LOGGER.info("Removed waypoint: {}", payload.color());
                } else {
                    // Create TrackedWaypoint for client-side tracking
                    UUID playerUuid = context.player().getUuid();
                    Waypoint.Config config = new Waypoint.Config();
                    config.color = java.util.Optional.of(payload.colorInt());
                    Vec3i pos = new Vec3i(payload.x(), payload.y(), payload.z());
                    TrackedWaypoint waypoint = TrackedWaypoint.ofPos(playerUuid, config, pos);
                    clientWaypoints.put(payload.color(), new ClientMarker(waypoint, pos));
                    LOGGER.info("Received waypoint: color={}, pos=({},{},{})", payload.color(), payload.x(), payload.y(), payload.z());
                }
            });
        });

        // Register showDistance receiver
        ClientPlayNetworking.registerGlobalReceiver(HeadingMarkerMod.ShowDistanceSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                HeadingMarkerMod.setShowDistance(context.player().getUuid(), payload.show());
                LOGGER.info("Received showDistance sync: {}", payload.show());
            });
        });

        // Clear waypoints on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clientWaypoints.clear();
            LOGGER.info("Cleared client waypoints on disconnect");
        });
    }
}
