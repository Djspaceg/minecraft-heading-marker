# Heading Marker - Set Coordinates Helper
# This function provides instructions for setting a marker at specific coordinates

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"How to set a marker at specific coordinates:","color":"yellow"}]
tellraw @s [""]
tellraw @s ["",{"text":"Step 1: ","color":"aqua","bold":true},{"text":"Set the X, Y, Z coordinates:","color":"white"}]
tellraw @s ["",{"text":"  /scoreboard players set @s hm.x <value>","color":"gray","clickEvent":{"action":"suggest_command","value":"/scoreboard players set @s hm.x "}}]
tellraw @s ["",{"text":"  /scoreboard players set @s hm.y <value>","color":"gray","clickEvent":{"action":"suggest_command","value":"/scoreboard players set @s hm.y "}}]
tellraw @s ["",{"text":"  /scoreboard players set @s hm.z <value>","color":"gray","clickEvent":{"action":"suggest_command","value":"/scoreboard players set @s hm.z "}}]
tellraw @s [""]
tellraw @s ["",{"text":"Step 2: ","color":"aqua","bold":true},{"text":"Activate the marker:","color":"white"}]
tellraw @s ["",{"text":"  /function heading_marker:set_marker_at","color":"gray","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set_marker_at"}}]
tellraw @s [""]
tellraw @s ["",{"text":"Quick tip: ","color":"gold","bold":true},{"text":"Or use ","color":"yellow"},{"text":"/function heading_marker:set_marker","color":"aqua"},{"text":" to mark your current location!","color":"yellow"}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.2
