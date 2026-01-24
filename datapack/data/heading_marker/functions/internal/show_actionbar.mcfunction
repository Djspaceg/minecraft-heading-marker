# Internal: Show Actionbar with all markers
# Build display showing only active markers

# Show based on which markers are active
execute if score @s hm.red.active matches 1 if score @s hm.blue.active matches 0 if score @s hm.green.active matches 0 if score @s hm.yellow.active matches 0 if score @s hm.purple.active matches 0 run title @s actionbar ["",{"text":"🔴 ","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"}]

execute if score @s hm.red.active matches 0 if score @s hm.blue.active matches 1 if score @s hm.green.active matches 0 if score @s hm.yellow.active matches 0 if score @s hm.purple.active matches 0 run title @s actionbar ["",{"text":"🔵 ","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}]

execute if score @s hm.red.active matches 1 if score @s hm.blue.active matches 1 if score @s hm.green.active matches 0 if score @s hm.yellow.active matches 0 if score @s hm.purple.active matches 0 run title @s actionbar ["",{"text":"🔴","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"},{"text":" 🔵","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}]

# For simplicity with many combinations, show all and let zeros indicate inactive
execute if score @s hm.temp matches 2.. run title @s actionbar ["",\
{"text":"🔴","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"},\
{"text":" 🔵","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"},\
{"text":" 🟢","color":"green"},{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"},\
{"text":" 🟡","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"},\
{"text":" 🟣","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}]
