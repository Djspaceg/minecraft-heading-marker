# Validate color input (0-4)
execute unless score @s hm.input.color matches 0..4 run tellraw @s {"text":"Error: Color must be 0-4 (red/blue/green/yellow/purple)","color":"red"}
execute unless score @s hm.input.color matches 0..4 run return fail

# Get player UUID for entity tagging
function headingmarker:internal/get_player_uuid

# Remove existing waypoint of this color if it exists
execute if score @s hm.input.color matches 0 if score @s hm.red.active matches 1 run function headingmarker:internal/colors/remove_red
execute if score @s hm.input.color matches 1 if score @s hm.blue.active matches 1 run function headingmarker:internal/colors/remove_blue
execute if score @s hm.input.color matches 2 if score @s hm.green.active matches 1 run function headingmarker:internal/colors/remove_green
execute if score @s hm.input.color matches 3 if score @s hm.yellow.active matches 1 run function headingmarker:internal/colors/remove_yellow
execute if score @s hm.input.color matches 4 if score @s hm.purple.active matches 1 run function headingmarker:internal/colors/remove_purple

# Spawn new waypoint of the specified color
execute if score @s hm.input.color matches 0 run function headingmarker:internal/colors/spawn_red
execute if score @s hm.input.color matches 1 run function headingmarker:internal/colors/spawn_blue
execute if score @s hm.input.color matches 2 run function headingmarker:internal/colors/spawn_green
execute if score @s hm.input.color matches 3 run function headingmarker:internal/colors/spawn_yellow
execute if score @s hm.input.color matches 4 run function headingmarker:internal/colors/spawn_purple
