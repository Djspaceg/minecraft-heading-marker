# Count how many waypoint entities exist for this player
execute store result score @s hm.waypoint.count if entity @e[type=armor_stand,tag=hm.waypoint,distance=..999999]

# If count is 0 but player thinks they have a waypoint, it was destroyed
execute if score @s hm.waypoint.count matches 0 run function headingmarker:internal/waypoint_lost
