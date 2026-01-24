# Example: Mark village with yellow marker
# This is a template - edit the coordinates to match your village!

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Example: Setting yellow marker for village","color":"yellow"}]
tellraw @s ["",{"text":"Edit this command with your village coordinates:","color":"gray"}]
tellraw @s ["",{"text":"/function heading_marker:set {x:800, y:72, z:-600, color:3}","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set {x:800, y:72, z:-600, color:3}"},"hoverEvent":{"action":"show_text","contents":"Click to edit and run"}}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1
