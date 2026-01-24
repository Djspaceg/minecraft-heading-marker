# Heading Marker - Remove Marker Command
# Usage: Set color first, then call this
# /scoreboard players set @s hm.input.color <0-4>
# /function heading_marker:marker_remove

# Check if color is set
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Error: Color not specified!","color":"red"}]
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"Use: /scoreboard players set @s hm.input.color <0-4>","color":"yellow"}]
execute unless score @s hm.input.color matches 0..4 run tellraw @s ["",{"text":"Colors: 0=red, 1=blue, 2=green, 3=yellow, 4=purple","color":"gray"}]
execute unless score @s hm.input.color matches 0..4 run return 0

# Remove marker based on color
execute if score @s hm.input.color matches 0 run function heading_marker:internal/remove_red
execute if score @s hm.input.color matches 1 run function heading_marker:internal/remove_blue
execute if score @s hm.input.color matches 2 run function heading_marker:internal/remove_green
execute if score @s hm.input.color matches 3 run function heading_marker:internal/remove_yellow
execute if score @s hm.input.color matches 4 run function heading_marker:internal/remove_purple

# Save markers to storage
function heading_marker:save_markers

# Clear input
scoreboard players reset @s hm.input.color
