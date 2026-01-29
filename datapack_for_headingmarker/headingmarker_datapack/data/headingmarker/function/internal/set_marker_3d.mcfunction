# Internal: Set Marker 3D Mode
# All three coordinates are set in hm.input.x/y/z
# Auto-cycle color if not specified

# If color not specified, find next available color
execute unless score @s hm.input.color matches 0..4 run function headingmarker:internal/auto_select_color

# Validate color is in range
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Error: Invalid color! Use 0-4 (red/blue/green/yellow/purple)","color":"red"}]
execute unless score @s hm.input.color matches 0..4 run return 0

# Set marker based on color
execute if score @s hm.input.color matches 0 run function headingmarker:internal/set_red
execute if score @s hm.input.color matches 1 run function headingmarker:internal/set_blue
execute if score @s hm.input.color matches 2 run function headingmarker:internal/set_green
execute if score @s hm.input.color matches 3 run function headingmarker:internal/set_yellow
execute if score @s hm.input.color matches 4 run function headingmarker:internal/set_purple

# Save markers to storage
function headingmarker:save_markers

# Clear input variables
scoreboard players reset @s hm.input.x
scoreboard players reset @s hm.input.y
scoreboard players reset @s hm.input.z
scoreboard players reset @s hm.input.color


