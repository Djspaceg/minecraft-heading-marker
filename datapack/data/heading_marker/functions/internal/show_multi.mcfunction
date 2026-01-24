# Internal: Show Multiple Markers
# Builds compact display of only active markers

# Build text component dynamically
data modify storage heading_marker:display parts set value []

# Add each active marker to parts array
execute if score @s hm.red.active matches 1 run data modify storage heading_marker:display parts append value '{"text":"🔴","color":"red"}'
execute if score @s hm.red.active matches 1 run function heading_marker:internal/add_red_dist

execute if score @s hm.blue.active matches 1 run data modify storage heading_marker:display parts append value '{"text":" 🔵","color":"blue"}'
execute if score @s hm.blue.active matches 1 run function heading_marker:internal/add_blue_dist

execute if score @s hm.green.active matches 1 run data modify storage heading_marker:display parts append value '{"text":" 🟢","color":"green"}'
execute if score @s hm.green.active matches 1 run function heading_marker:internal/add_green_dist

execute if score @s hm.yellow.active matches 1 run data modify storage heading_marker:display parts append value '{"text":" 🟡","color":"yellow"}'
execute if score @s hm.yellow.active matches 1 run function heading_marker:internal/add_yellow_dist

execute if score @s hm.purple.active matches 1 run data modify storage heading_marker:display parts append value '{"text":" 🟣","color":"light_purple"}'
execute if score @s hm.purple.active matches 1 run function heading_marker:internal/add_purple_dist

# Display with macro (simplified version - show all active)
# For now, use direct display since macros are 1.20.2+
execute if score @s hm.red.active matches 1 if score @s hm.blue.active matches 0 if score @s hm.green.active matches 0 if score @s hm.yellow.active matches 0 if score @s hm.purple.active matches 0 run title @s actionbar ["",{"text":"🔴 ","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"}]

execute if score @s hm.red.active matches 1 if score @s hm.blue.active matches 1 run title @s actionbar ["",{"text":"🔴","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"},{"text":" 🔵","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}]

# Fallback: show all active (with zeroes for inactive - acceptable compromise)
execute if score @s hm.temp matches 3.. run title @s actionbar ["",{"text":"🔴","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"},{"text":" 🔵","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"},{"text":" 🟢","color":"green"},{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"},{"text":" 🟡","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"},{"text":" 🟣","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}]
