# Heading Marker - Display All Markers
# Shows all active waypoint markers on the player's HUD

# Count active markers
scoreboard players set @s hm.temp 0
execute if score @s hm.red.active matches 1 run scoreboard players add @s hm.temp 1
execute if score @s hm.blue.active matches 1 run scoreboard players add @s hm.temp 1
execute if score @s hm.green.active matches 1 run scoreboard players add @s hm.temp 1
execute if score @s hm.yellow.active matches 1 run scoreboard players add @s hm.temp 1
execute if score @s hm.purple.active matches 1 run scoreboard players add @s hm.temp 1

# If no markers active, clear actionbar
execute if score @s hm.temp matches 0 run title @s actionbar ""
execute if score @s hm.temp matches 0 run return 0

# Calculate distances for all active markers
execute if score @s hm.red.active matches 1 run function headingmarker:internal/calc_red
execute if score @s hm.blue.active matches 1 run function headingmarker:internal/calc_blue
execute if score @s hm.green.active matches 1 run function headingmarker:internal/calc_green
execute if score @s hm.yellow.active matches 1 run function headingmarker:internal/calc_yellow
execute if score @s hm.purple.active matches 1 run function headingmarker:internal/calc_purple

# Display combined actionbar - show markers with icons and distances
function headingmarker:internal/show_actionbar


