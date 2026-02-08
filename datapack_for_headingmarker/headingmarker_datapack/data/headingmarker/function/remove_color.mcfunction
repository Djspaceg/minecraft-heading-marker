# Validate color input
execute unless score @s hm.input.color matches 0..4 run tellraw @s {"text":"Error: Color must be 0-4","color":"red"}
execute unless score @s hm.input.color matches 0..4 run return fail

# Get player UUID
function headingmarker:internal/get_player_uuid

# Store UUID in storage
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Define color ID to remove
# Red (0)
execute if score @s hm.input.color matches 0 run data modify storage headingmarker:temp color_id set value 0
execute if score @s hm.input.color matches 0 run scoreboard players set @s hm.red.active 0

# Blue (1)
execute if score @s hm.input.color matches 1 run data modify storage headingmarker:temp color_id set value 1
execute if score @s hm.input.color matches 1 run scoreboard players set @s hm.blue.active 0

# Green (2)
execute if score @s hm.input.color matches 2 run data modify storage headingmarker:temp color_id set value 2
execute if score @s hm.input.color matches 2 run scoreboard players set @s hm.green.active 0

# Yellow (3)
execute if score @s hm.input.color matches 3 run data modify storage headingmarker:temp color_id set value 3
execute if score @s hm.input.color matches 3 run scoreboard players set @s hm.yellow.active 0

# Purple (4)
execute if score @s hm.input.color matches 4 run data modify storage headingmarker:temp color_id set value 4
execute if score @s hm.input.color matches 4 run scoreboard players set @s hm.purple.active 0

# Remove waypoint of specified color
function headingmarker:internal/remove_waypoint_macro with storage headingmarker:temp
