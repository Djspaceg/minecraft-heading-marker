# Internal: Remove Purple Marker
scoreboard players set @s hm.purple.active 0
scoreboard players reset @s hm.purple.x
scoreboard players reset @s hm.purple.y
scoreboard players reset @s hm.purple.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟣 Purple marker removed","color":"light_purple"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8
