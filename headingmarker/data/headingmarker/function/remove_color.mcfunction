# Validate color input
execute unless score @s hm.input.color matches 0..4 run tellraw @s {"text":"Error: Color must be 0-4","color":"red"}
execute unless score @s hm.input.color matches 0..4 run return fail

# Get player UUID
function headingmarker:internal/get_player_uuid

# Remove waypoint of specified color
execute if score @s hm.input.color matches 0 run function headingmarker:internal/colors/remove_red
execute if score @s hm.input.color matches 1 run function headingmarker:internal/colors/remove_blue
execute if score @s hm.input.color matches 2 run function headingmarker:internal/colors/remove_green
execute if score @s hm.input.color matches 3 run function headingmarker:internal/colors/remove_yellow
execute if score @s hm.input.color matches 4 run function headingmarker:internal/colors/remove_purple
