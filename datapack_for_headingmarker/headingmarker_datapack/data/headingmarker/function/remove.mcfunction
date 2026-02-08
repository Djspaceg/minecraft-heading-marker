# Get player UUID
function headingmarker:internal/get_player_uuid

# Store UUID in storage for the macro
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Kill all waypoint entities for this player
function headingmarker:internal/remove_all_macro with storage headingmarker:temp

# Reset all active flags
scoreboard players set @s hm.red.active 0
scoreboard players set @s hm.blue.active 0
scoreboard players set @s hm.green.active 0
scoreboard players set @s hm.yellow.active 0
scoreboard players set @s hm.purple.active 0
scoreboard players set @s hm.has.waypoint 0

tellraw @s {"text":"Removed all waypoints.","color":"green"}
