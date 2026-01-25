package com.djspaceg.headingmarker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.djspaceg.headingmarker.Waypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HeadingMarkerClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("headingmarker-client");
    
    private static final Map<String, Waypoint> clientWaypoints = new HashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Heading Marker Client...");
        
        ClientPlayNetworking.registerGlobalReceiver(WaypointSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                clientWaypoints.clear();
                clientWaypoints.putAll(payload.waypoints());
            });
        });
    }
    
    /**
     * Called by ExperienceBarMixin to render waypoint markers.
     * This is invoked during the ExperienceBar's renderAddons phase,
     * ensuring markers are drawn exactly where vanilla player indicators appear.
     */
    public static void renderWaypointMarkers(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (clientWaypoints.isEmpty()) return;
        
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int cx = screenWidth / 2;
        
        // XP bar is at bottom of screen, typically at height - 32 (above hotbar)
        // Player indicators appear at height - 39 (just above XP bar)
        int xpBarY = screenHeight - 32;
        int indicatorY = screenHeight - 39;
        
        float playerYaw = client.player.getYaw() % 360;
        if (playerYaw < 0) playerYaw += 360;
        
        // XP bar width is 182 pixels (vanilla size)
        int barHalfWidth = 91; // 182 / 2
        float maxAngle = 90.0f; // Show markers within 90 degrees left/right
        float pixelsPerDegree = barHalfWidth / maxAngle;

        for (Map.Entry<String, Waypoint> entry : clientWaypoints.entrySet()) {
            String colorName = entry.getKey();
            Waypoint wp = entry.getValue();
            
            if (!wp.active) continue;
            
            if (client.world == null || 
                !client.world.getRegistryKey().getValue().toString().equals(wp.dimension)) {
                 continue;
            }
            
            double dX = wp.x - client.player.getX();
            double dZ = wp.z - client.player.getZ();
            
            double angleRad = Math.atan2(dZ, dX);
            double angleDeg = Math.toDegrees(angleRad) - 90; 
            
            double diff = angleDeg - playerYaw;
            while (diff < -180) diff += 360;
            while (diff > 180) diff -= 360;
            
            // Show markers within 180 degree view (full bar width)
            if (Math.abs(diff) <= maxAngle) {
                float xOffset = (float) (diff * pixelsPerDegree);
                int renderX = (int) (cx + xOffset) - 3; // Center 6px marker
                
                int colorInt = getColorInt(colorName);
                
                // Draw marker on XP bar (small vertical bar, similar to player indicators)
                context.fill(renderX, indicatorY, renderX + 6, indicatorY + 8, colorInt | 0xFF000000);
                // Add slight shadow/outline for visibility
                context.fill(renderX - 1, indicatorY - 1, renderX + 7, indicatorY, 0xFF000000);
                context.fill(renderX - 1, indicatorY + 8, renderX + 7, indicatorY + 9, 0xFF000000);
            }
        }
    }
    
    private static int getColorInt(String color) {
        switch (color.toLowerCase()) {
            case "red": return 0xFF0000;
            case "blue": return 0x5555FF;
            case "green": return 0x55FF55;
            case "yellow": return 0xFFFF55;
            case "purple": return 0xFF55FF;
            default: return 0xFFFFFF;
        }
    }
}
