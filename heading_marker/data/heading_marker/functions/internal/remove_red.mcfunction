# Internal: Remove Red Marker
scoreboard players set @s hm.red.active 0
scoreboard players reset @s hm.red.x
scoreboard players reset @s hm.red.y
scoreboard players reset @s hm.red.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🔴 Red marker removed","color":"red"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8
