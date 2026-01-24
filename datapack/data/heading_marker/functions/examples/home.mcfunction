# Example: Mark home base with red marker
# This is a template - edit the coordinates to match your actual home!

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Example: Setting red marker for home base","color":"yellow"}]
tellraw @s ["",{"text":"Edit this command with your actual coordinates:","color":"gray"}]
tellraw @s ["",{"text":"/function heading_marker:set {x:0, y:64, z:0, color:0}","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set {x:0, y:64, z:0, color:0}"},"hoverEvent":{"action":"show_text","contents":"Click to edit and run"}}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1
