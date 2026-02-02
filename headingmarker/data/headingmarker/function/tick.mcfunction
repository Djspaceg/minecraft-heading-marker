# Check if any player's waypoint entity was destroyed
execute as @a[scores={hm.has.waypoint=1}] run function headingmarker:internal/check_waypoint_health
