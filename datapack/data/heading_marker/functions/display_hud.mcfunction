# Heading Marker - Display HUD
# Shows the waypoint marker on the player's HUD

# Calculate distance and direction
execute store result score @s hm.dx run data get entity @s Pos[0]
execute store result score @s hm.temp run data get entity @s Pos[2]

# Calculate delta X and Z
scoreboard players operation @s hm.dx -= @s hm.x
scoreboard players operation @s hm.dz = @s hm.temp
scoreboard players operation @s hm.dz -= @s hm.z

# Calculate approximate distance (simplified)
scoreboard players operation @s hm.dist = @s hm.dx
scoreboard players operation @s hm.dist *= @s hm.dx
scoreboard players operation @s hm.temp = @s hm.dz
scoreboard players operation @s hm.temp *= @s hm.dz
scoreboard players operation @s hm.dist += @s hm.temp

# Display HUD based on color
execute if score @s hm.color matches 0 run title @s actionbar ["",{"text":"🔴 ","color":"red"},{"text":"Waypoint: ","color":"gold"},{"score":{"name":"@s","objective":"hm.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.z"},"color":"white"},{"text":" | Distance²: ","color":"gray"},{"score":{"name":"@s","objective":"hm.dist"},"color":"yellow"}]

execute if score @s hm.color matches 1 run title @s actionbar ["",{"text":"🔵 ","color":"blue"},{"text":"Waypoint: ","color":"gold"},{"score":{"name":"@s","objective":"hm.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.z"},"color":"white"},{"text":" | Distance²: ","color":"gray"},{"score":{"name":"@s","objective":"hm.dist"},"color":"yellow"}]

execute if score @s hm.color matches 2 run title @s actionbar ["",{"text":"🟢 ","color":"green"},{"text":"Waypoint: ","color":"gold"},{"score":{"name":"@s","objective":"hm.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.z"},"color":"white"},{"text":" | Distance²: ","color":"gray"},{"score":{"name":"@s","objective":"hm.dist"},"color":"yellow"}]

execute if score @s hm.color matches 3 run title @s actionbar ["",{"text":"🟡 ","color":"yellow"},{"text":"Waypoint: ","color":"gold"},{"score":{"name":"@s","objective":"hm.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.z"},"color":"white"},{"text":" | Distance²: ","color":"gray"},{"score":{"name":"@s","objective":"hm.dist"},"color":"yellow"}]

execute if score @s hm.color matches 4 run title @s actionbar ["",{"text":"🟣 ","color":"light_purple"},{"text":"Waypoint: ","color":"gold"},{"score":{"name":"@s","objective":"hm.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.z"},"color":"white"},{"text":" | Distance²: ","color":"gray"},{"score":{"name":"@s","objective":"hm.dist"},"color":"yellow"}]
