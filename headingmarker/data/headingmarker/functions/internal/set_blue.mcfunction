# Internal: Set Blue Marker
scoreboard players operation @s hm.blue.x = @s hm.input.x
scoreboard players operation @s hm.blue.y = @s hm.input.y
scoreboard players operation @s hm.blue.z = @s hm.input.z
scoreboard players set @s hm.blue.active 1

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🔵 Blue marker set at ","color":"blue"},{"score":{"name":"@s","objective":"hm.blue.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.blue.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.blue.z"},"color":"white"}]
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.2


