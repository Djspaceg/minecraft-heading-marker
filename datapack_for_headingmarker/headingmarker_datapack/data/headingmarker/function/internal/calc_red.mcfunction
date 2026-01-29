# Internal: Calculate Red Marker Distance
execute store result score @s hm.dx run data get entity @s Pos[0]
execute store result score @s hm.temp run data get entity @s Pos[2]

scoreboard players operation @s hm.dx -= @s hm.red.x
scoreboard players operation @s hm.dz = @s hm.temp
scoreboard players operation @s hm.dz -= @s hm.red.z

scoreboard players operation @s hm.dist = @s hm.dx
scoreboard players operation @s hm.dist *= @s hm.dx
scoreboard players operation @s hm.temp = @s hm.dz
scoreboard players operation @s hm.temp *= @s hm.dz
scoreboard players operation @s hm.dist += @s hm.temp

# Store distance in red.dist for display
execute store result score @s hm.red.dist run scoreboard players get @s hm.dist


