# Internal: Remove Blue Marker
scoreboard players set @s hm.blue.active 0
scoreboard players reset @s hm.blue.x
scoreboard players reset @s hm.blue.y
scoreboard players reset @s hm.blue.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🔵 Blue marker removed","color":"blue"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8


