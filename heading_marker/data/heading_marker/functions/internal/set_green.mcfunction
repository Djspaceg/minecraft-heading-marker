# Internal: Set Green Marker
scoreboard players operation @s hm.green.x = @s hm.input.x
scoreboard players operation @s hm.green.y = @s hm.input.y
scoreboard players operation @s hm.green.z = @s hm.input.z
scoreboard players set @s hm.green.active 1

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"🟢 Green marker set at ","color":"green"},{"score":{"name":"@s","objective":"hm.green.x"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.green.y"},"color":"white"},{"text":" ","color":"white"},{"score":{"name":"@s","objective":"hm.green.z"},"color":"white"}]
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.0
