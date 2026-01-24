# Internal: Show Multiple Markers (Simplified)
# Displays only active markers - no combination checking needed
# Just sequentially add each active marker to the display

# Initialize empty display
data modify storage headingmarker:display text set value [""]

# Conditionally add each active marker
execute if score @s hm.red.active matches 1 run function headingmarker:internal/append_red
execute if score @s hm.blue.active matches 1 run function headingmarker:internal/append_blue
execute if score @s hm.green.active matches 1 run function headingmarker:internal/append_green
execute if score @s hm.yellow.active matches 1 run function headingmarker:internal/append_yellow
execute if score @s hm.purple.active matches 1 run function headingmarker:internal/append_purple

# Display the built text
title @s actionbar {"nbt":"text","storage":"headingmarker:display","interpret":true}


