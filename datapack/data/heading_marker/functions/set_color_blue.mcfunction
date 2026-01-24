# Heading Marker - Set Color
# Changes the color of your waypoint marker

scoreboard players set @s hm.color 1
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Marker color set to ","color":"yellow"},{"text":"🔵 Blue","color":"blue"}]
playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.2
