# Internal: Set Red Marker
scoreboard players operation @s hm.red.x = @s hm.input.x
scoreboard players operation @s hm.red.y = @s hm.input.y
scoreboard players operation @s hm.red.z = @s hm.input.z
scoreboard players set @s hm.red.active 1

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🔴 Red marker set at ","color":"red"},{"score":{"name":"@s","objective":"hm.red.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.red.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.red.z"},"color":"white"}]
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5


