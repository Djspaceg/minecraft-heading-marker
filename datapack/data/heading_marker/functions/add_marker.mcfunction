# Heading Marker - Add Marker (Legacy)
# This function is for backward compatibility
# Redirects to the new HUD marker system

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"HUD Marker System","color":"aqua"}]
tellraw @s [""]
tellraw @s ["",{"text":"This data pack now shows markers on your HUD!","color":"yellow"}]
tellraw @s [""]
tellraw @s ["",{"text":"Available commands:","color":"gold","bold":true}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"/function heading_marker:set_marker","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set_marker"}},{"text":" - Mark current location","color":"white"}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"/function heading_marker:help_coordinates","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:help_coordinates"}},{"text":" - Set coordinates manually","color":"white"}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"/function heading_marker:clear_marker","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:clear_marker"}},{"text":" - Remove marker","color":"white"}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"/function heading_marker:set_color_*","color":"aqua"},{"text":" - Change marker color (red/blue/green/yellow/purple)","color":"white"}]
tellraw @s [""]
tellraw @s ["",{"text":"The marker will appear on your actionbar showing distance and coordinates!","color":"green"}]

playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5
