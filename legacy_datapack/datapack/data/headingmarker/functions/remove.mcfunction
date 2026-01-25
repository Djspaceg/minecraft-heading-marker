# Heading Marker - Remove Marker Command (Macro Version)
# Usage with macros (Minecraft 1.20.2+):
#   /function headingmarker:remove {color:0}   # Remove red marker
#   /function headingmarker:remove {color:1}   # Remove blue marker
#
# Colors: 0=red, 1=blue, 2=green, 3=yellow, 4=purple

# Store color parameter
$scoreboard players set @s hm.input.color $(color)

# Validate color is in range
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Error: Invalid color!","color":"red"}]
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"Colors: 0=red, 1=blue, 2=green, 3=yellow, 4=purple","color":"gray"}]
execute unless score @s hm.input.color matches 0..4 run return 0

# Remove marker based on color
execute if score @s hm.input.color matches 0 run function headingmarker:internal/remove_red
execute if score @s hm.input.color matches 1 run function headingmarker:internal/remove_blue
execute if score @s hm.input.color matches 2 run function headingmarker:internal/remove_green
execute if score @s hm.input.color matches 3 run function headingmarker:internal/remove_yellow
execute if score @s hm.input.color matches 4 run function headingmarker:internal/remove_purple

# Save markers to storage
function headingmarker:save_markers

# Clear input
scoreboard players reset @s hm.input.color


