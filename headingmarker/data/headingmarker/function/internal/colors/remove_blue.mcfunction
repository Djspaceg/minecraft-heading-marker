# Check if waypoint exists
execute unless score @s hm.blue.active matches 1 run tellraw @s {"text":"No blue waypoint to remove","color":"red"}
execute unless score @s hm.blue.active matches 1 run return fail

# Store player UUID for entity targeting
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Kill player's blue waypoint entity
function headingmarker:internal/colors/kill_blue_macro with storage headingmarker:temp

# Mark as inactive
scoreboard players set @s hm.blue.active 0

# Confirm
tellraw @s {"text":"🔵 Blue waypoint removed","color":"yellow"}
