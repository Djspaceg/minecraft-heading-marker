# Check if waypoint exists
execute unless score @s hm.purple.active matches 1 run tellraw @s {"text":"No purple waypoint to remove","color":"red"}
execute unless score @s hm.purple.active matches 1 run return fail

# Store player UUID for entity targeting
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Kill player's purple waypoint entity
function headingmarker:internal/colors/kill_purple_macro with storage headingmarker:temp

# Mark as inactive
scoreboard players set @s hm.purple.active 0

# Confirm
tellraw @s {"text":"🟣 Purple waypoint removed","color":"yellow"}
