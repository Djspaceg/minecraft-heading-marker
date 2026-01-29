# Example: Mark nether portal with purple marker
# This is a template - edit the coordinates to match your portal!

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Example: Setting purple marker for portal","color":"yellow"}]
tellraw @s ["",{"text":"Edit this command with your portal coordinates:","color":"gray"}]
tellraw @s ["",{"text":"/function headingmarker:set {x:500, y:70, z:300, color:4}","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function headingmarker:set {x:500, y:70, z:300, color:4}"},"hoverEvent":{"action":"show_text","contents":"Click to edit and run"}}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1


