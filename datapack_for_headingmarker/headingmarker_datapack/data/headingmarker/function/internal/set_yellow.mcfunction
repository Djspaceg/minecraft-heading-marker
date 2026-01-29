# Internal: Set Yellow Marker
scoreboard players operation @s hm.yellow.x = @s hm.input.x
scoreboard players operation @s hm.yellow.y = @s hm.input.y
scoreboard players operation @s hm.yellow.z = @s hm.input.z
scoreboard players set @s hm.yellow.active 1

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟡 Yellow marker set at ","color":"yellow"},{"score":{"name":"@s","objective":"hm.yellow.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.yellow.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.yellow.z"},"color":"white"}]
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 0.8


