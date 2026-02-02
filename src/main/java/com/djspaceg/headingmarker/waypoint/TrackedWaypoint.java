package com.djspaceg.headingmarker.waypoint;

import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import java.util.UUID;

public abstract class TrackedWaypoint implements Waypoint {
    private final UUID owner;
    private final Config config;
    protected final Vec3i pos;

    protected TrackedWaypoint(UUID owner, Config config, Vec3i pos) {
        this.owner = owner;
        this.config = config;
        this.pos = pos;
    }

    public static TrackedWaypoint ofPos(UUID owner, Config config, Vec3i pos) {
        return new TrackedWaypoint(owner, config, pos) {
            @Override
            public Config getConfig() {
                return super.config;
            }
        };
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public interface YawProvider {
        float getCameraYaw();
        Vec3d getCameraPos();
    }

    public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
        Vec3d camPos = yawProvider.getCameraPos();
        double dX = this.pos.getX() + 0.5 - camPos.x;
        double dZ = this.pos.getZ() + 0.5 - camPos.z;

        // Calculate angle to target
        // MC Yaw: 0=South (Z+), then increases clockwise (South->West->North->East)
        // atan2(dz, dx) gives angle from X axis CCW.
        // We want angle relative to look direction.

        double angleRad = Math.atan2(dZ, dX);
        double angleDeg = Math.toDegrees(angleRad) - 90.0;

        double camYaw = yawProvider.getCameraYaw();

        return MathHelper.wrapDegrees(angleDeg - camYaw);
    }
}
