package com.djspaceg.headingmarker;

import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.Waypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Helper class to store both waypoint and position
class ClientMarker {
    public final TrackedWaypoint waypoint;
    public final Vec3i pos;
    public ClientMarker(TrackedWaypoint waypoint, Vec3i pos) {
        this.waypoint = waypoint;
        this.pos = pos;
    }
}

public class HeadingMarkerClient implements ClientModInitializer {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("headingmarker-client");
    
    // Client-side waypoint storage: color -> ClientMarker
    private static final Map<String, ClientMarker> clientWaypoints = new HashMap<>();

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
    }
    
    /**
     * Called by ExperienceBarMixin to render waypoint markers.
     * Uses vanilla ClientWaypointHandler and TrackedWaypoint.getRelativeYaw() for proper integration.
     */
    public static void renderWaypointMarkers(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // Early exit if no waypoints
        if (clientWaypoints.isEmpty()) return;
        
        // ...existing code...
        
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int cx = screenWidth / 2;
        // Shift marker down to sit directly on the XP bar (vanilla bar is at y = screenHeight - 32)
        int indicatorY = screenHeight - 32; // Sits on top of XP bar
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
            public net.minecraft.util.math.Vec3d getCameraPos() {
                return client.player.getEyePos();
            }
        };
        
        // Iterate over our client-side waypoints
        for (ClientMarker marker : clientWaypoints.values()) {
            TrackedWaypoint waypoint = marker.waypoint;
            Vec3i pos = marker.pos;
            // Use vanilla's yaw calculation - this handles all the math for us!
            double relativeYaw = waypoint.getRelativeYaw(client.world, yawProvider, tickProgress);

            // Calculate distance to marker (horizontal only)
            double dx = pos.getX() + 0.5 - client.player.getX();
            double dz = pos.getZ() + 0.5 - client.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            // Scale: full at 128, 50% at 384, 25% at 1024+
            float scale;
            if (dist <= 128) {
                scale = 1.0f;
            } else if (dist <= 384) {
                scale = 1.0f - 0.5f * (float)((dist - 128) / 256.0);
            } else if (dist <= 1024) {
                scale = 0.5f - 0.25f * (float)((dist - 384) / 640.0);
            } else {
                scale = 0.25f;
            }
            // Clamp
            if (scale < 0.18f) scale = 0.18f;

            // Convert yaw to screen position
            // relativeYaw is in degrees, where 0 = straight ahead, -90 = left, +90 = right
            if (Math.abs(relativeYaw) <= maxAngle) {
                float pixelsPerDegree = barHalfWidth / maxAngle;
                float xOffset = (float) relativeYaw * pixelsPerDegree;
                // Marker size (square): 8px at full scale
                int markerSize = Math.round(8 * scale);
                if (markerSize < 3) markerSize = 3;
                int renderX = (int) (cx + xOffset) - markerSize / 2;
                int renderY = indicatorY - markerSize / 2;

                // Get custom color from waypoint config
                int colorInt = waypoint.getConfig().color.orElse(0xFFFFFF);

                // Draw marker as a square
                context.fill(renderX, renderY, renderX + markerSize, renderY + markerSize, colorInt | 0xFF000000);
                // Add slight shadow/outline for visibility
                context.fill(renderX - 1, renderY - 1, renderX + markerSize + 1, renderY, 0xFF000000);
                context.fill(renderX - 1, renderY + markerSize, renderX + markerSize + 1, renderY + markerSize + 1, 0xFF000000);
            }
        }
    }
}
