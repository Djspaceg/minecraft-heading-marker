# Internal: Show Multiple Markers
# Displays markers based on which are active (handles all combinations)

# Show 1 marker (clean display)
execute if score @s hm.temp matches 1 if score @s hm.red.active matches 1 run title @s actionbar ["",{"text":"🔴 ","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.blue.active matches 1 run title @s actionbar ["",{"text":"🔵 ","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.green.active matches 1 run title @s actionbar ["",{"text":"🟢 ","color":"green"},{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.yellow.active matches 1 run title @s actionbar ["",{"text":"🟡 ","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"}]
execute if score @s hm.temp matches 1 if score @s hm.purple.active matches 1 run title @s actionbar ["",{"text":"🟣 ","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}]

# Show 2+ markers - show all 5 slots, inactive markers will show 0 distance
# This is simpler and works for all combinations (0s indicate inactive markers)
execute if score @s hm.temp matches 2.. run title @s actionbar ["",{"text":"🔴","color":"red"},{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"},{"text":" 🔵","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"},{"text":" 🟢","color":"green"},{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"},{"text":" 🟡","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"},{"text":" 🟣","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}]
