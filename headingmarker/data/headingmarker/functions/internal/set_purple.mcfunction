# Internal: Set Purple Marker
scoreboard players operation @s hm.purple.x = @s hm.input.x
scoreboard players operation @s hm.purple.y = @s hm.input.y
scoreboard players operation @s hm.purple.z = @s hm.input.z
scoreboard players set @s hm.purple.active 1

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟣 Purple marker set at ","color":"light_purple"},{"score":{"name":"@s","objective":"hm.purple.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.purple.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.purple.z"},"color":"white"}]
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 0.6


