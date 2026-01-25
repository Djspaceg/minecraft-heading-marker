# Internal: Remove Yellow Marker
scoreboard players set @s hm.yellow.active 0
scoreboard players reset @s hm.yellow.x
scoreboard players reset @s hm.yellow.y
scoreboard players reset @s hm.yellow.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟡 Yellow marker removed","color":"yellow"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8


