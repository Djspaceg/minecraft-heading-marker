# Kill all waypoint entities tagged with this player's marker
# Note: In Phase 1 we just kill all waypoints, Phase 3+ will use player-specific tags
kill @e[type=armor_stand,tag=hm.waypoint,limit=1,sort=nearest]

# Clear the flag
scoreboard players set @s hm.has.waypoint 0

# Confirm removal
tellraw @s {"text":"Waypoint removed","color":"yellow"}
