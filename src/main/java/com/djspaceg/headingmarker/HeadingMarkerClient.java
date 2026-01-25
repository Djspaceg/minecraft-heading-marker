package com.djspaceg.headingmarker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.djspaceg.headingmarker.Waypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
        
        HudRenderCallback.EVENT.register(this::renderHud);
    }
    
    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (clientWaypoints.isEmpty()) return;
        
        int cx = context.getScaledWindowWidth() / 2;
        int yPos = 10; // Top compass bar position
        
        float playerYaw = client.player.getYaw() % 360;
        if (playerYaw < 0) playerYaw += 360;
        
        float fov = (float) client.options.getFov().getValue();
        float halfFov = fov / 2.0f;
        int barWidth = 200; 
        float pixelsPerDegree = barWidth / fov;

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
            
            if (diff >= -halfFov && diff <= halfFov) {
                float xOffset = (float) (diff * pixelsPerDegree);
                int renderX = (int) (cx + xOffset) - 2; // Center 4px wide bar
                
                int colorInt = getColorInt(colorName);
                
                // Draw a marker (Rectangle)
                // x1, y1, x2, y2, color
                context.fill(renderX, yPos, renderX + 4, yPos + 12, colorInt | 0xFF000000); 
            }
        }
    }
    
    private int getColorInt(String color) {
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
