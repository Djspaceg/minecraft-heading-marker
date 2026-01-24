# Internal: Calculate Blue Marker Distance
execute store result score @s hm.dx run data get entity @s Pos[0]
execute store result score @s hm.temp run data get entity @s Pos[2]

scoreboard players operation @s hm.dx -= @s hm.blue.x
scoreboard players operation @s hm.dz = @s hm.temp
scoreboard players operation @s hm.dz -= @s hm.blue.z

scoreboard players operation @s hm.dist = @s hm.dx
scoreboard players operation @s hm.dist *= @s hm.dx
scoreboard players operation @s hm.temp = @s hm.dz
scoreboard players operation @s hm.temp *= @s hm.dz
scoreboard players operation @s hm.dist += @s hm.temp

execute store result score @s hm.blue.dist run scoreboard players get @s hm.dist
