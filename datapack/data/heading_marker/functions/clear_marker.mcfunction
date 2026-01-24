# Heading Marker - Clear Marker
# Removes the waypoint marker from the HUD

scoreboard players set @s hm.active 0
scoreboard players reset @s hm.x
scoreboard players reset @s hm.y
scoreboard players reset @s hm.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Waypoint marker cleared!","color":"yellow"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8
