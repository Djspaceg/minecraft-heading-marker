# Internal: Remove Green Marker
scoreboard players set @s hm.green.active 0
scoreboard players reset @s hm.green.x
scoreboard players reset @s hm.green.y
scoreboard players reset @s hm.green.z

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟢 Green marker removed","color":"green"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8


