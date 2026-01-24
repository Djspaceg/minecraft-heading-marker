# Example: Mark mine location with blue marker
# This is a template - edit the coordinates to match your mine!

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Example: Setting blue marker for mine","color":"yellow"}]
tellraw @s ["",{"text":"Edit this command with your mine coordinates:","color":"gray"}]
tellraw @s ["",{"text":"/function heading_marker:set {x:100, y:12, z:-200, color:1}","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set {x:100, y:12, z:-200, color:1}"},"hoverEvent":{"action":"show_text","contents":"Click to edit and run"}}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1
