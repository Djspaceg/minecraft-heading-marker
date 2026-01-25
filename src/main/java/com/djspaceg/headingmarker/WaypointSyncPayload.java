package com.djspaceg.headingmarker;

import java.util.HashMap;
import java.util.Map;

import com.djspaceg.headingmarker.Waypoint;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaypointSyncPayload(Map<String, Waypoint> waypoints) implements CustomPayload {
    public static final CustomPayload.Id<WaypointSyncPayload> ID = 
        new CustomPayload.Id<>(Identifier.of("headingmarker", "sync"));
    
    public static final PacketCodec<RegistryByteBuf, WaypointSyncPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeInt(value.waypoints.size());
            value.waypoints.forEach((color, wp) -> {
                buf.writeString(color);
                buf.writeDouble(wp.x);
                buf.writeDouble(wp.y);
                buf.writeDouble(wp.z);
                buf.writeString(wp.dimension != null ? wp.dimension : "minecraft:overworld");
                buf.writeBoolean(wp.active);
            });
        },
        (buf) -> {
            int size = buf.readInt();
            Map<String, Waypoint> waypoints = new HashMap<>();
            for (int i = 0; i < size; i++) {
                String color = buf.readString();
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                String dim = buf.readString();
                boolean active = buf.readBoolean();
                
                Waypoint wp = new Waypoint(x, y, z, dim);
                wp.active = active;
                waypoints.put(color, wp);
            }
            return new WaypointSyncPayload(waypoints);
        }
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
