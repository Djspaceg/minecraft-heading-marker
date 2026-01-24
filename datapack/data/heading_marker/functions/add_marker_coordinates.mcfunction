# Heading Marker - Advanced: Set Marker at Coordinates
# This is a template function for advanced users
# To use: Modify the coordinates below and run this function

# Example: Set a marker at coordinates X=100, Y=64, Z=-200
# Note: This requires placing a lodestone at those coordinates
# You can use: /execute positioned 100 64 -200 run setblock ~ ~ ~ lodestone

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Advanced Coordinate Marker","color":"aqua"}]
tellraw @s ["",{"text":"To set a marker at specific coordinates:","color":"yellow"}]
tellraw @s ["",{"text":"1. ","color":"gray"},{"text":"Place a lodestone: ","color":"yellow"},{"text":"/execute positioned <x> <y> <z> run setblock ~ ~ ~ lodestone","color":"aqua","clickEvent":{"action":"suggest_command","value":"/execute positioned ~ ~ ~ run setblock ~ ~ ~ lodestone"}}]
tellraw @s ["",{"text":"2. ","color":"gray"},{"text":"Get a compass: ","color":"yellow"},{"text":"/give @s compass","color":"aqua","clickEvent":{"action":"suggest_command","value":"/give @s compass"}}]
tellraw @s ["",{"text":"3. ","color":"gray"},{"text":"Teleport there and right-click the lodestone with your compass","color":"yellow"}]

# Play a sound effect
playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.2
