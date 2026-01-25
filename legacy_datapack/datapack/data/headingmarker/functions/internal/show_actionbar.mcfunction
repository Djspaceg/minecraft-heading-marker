# Internal: Show Actionbar with all markers
# Build display showing only active markers

# Build display dynamically based on active markers
# Single marker displays
execute if score @s hm.temp matches 1 if score @s hm.red.active matches 1 run title @s actionbar ["",{"text":"🔴 ","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.blue.active matches 1 run title @s actionbar ["",{"text":"🔵 ","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.green.active matches 1 run title @s actionbar ["",{"text":"🟢 ","color":"green"},{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.yellow.active matches 1 run title @s actionbar ["",{"text":"🟡 ","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.purple.active matches 1 run title @s actionbar ["",{"text":"🟣 ","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}]

# Multiple markers - show all in compact format
execute if score @s hm.temp matches 2.. run function headingmarker:internal/show_multi


