# Check if waypoint exists
execute unless score @s hm.has.waypoint matches 1 run tellraw @s {"text":"No active waypoint to remove","color":"red"}

# If exists, remove it
execute if score @s hm.has.waypoint matches 1 run function headingmarker:internal/cleanup_waypoint
