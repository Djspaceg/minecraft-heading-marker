# Heading Marker - Set Color
# Changes the color of your waypoint marker
# Usage: /function heading_marker:set_color_red (or blue, green, yellow, purple)

scoreboard players set @s hm.color 0
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Marker color set to ","color":"yellow"},{"text":"🔴 Red","color":"red"}]
playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.2
