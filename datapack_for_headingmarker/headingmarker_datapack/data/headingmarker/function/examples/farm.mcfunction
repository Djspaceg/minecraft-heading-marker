# Example: Mark farm with green marker
# This is a template - edit the coordinates to match your farm!

tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Example: Setting green marker for farm","color":"yellow"}]
tellraw @s ["",{"text":"Edit this command with your farm coordinates:","color":"gray"}]
tellraw @s ["",{"text":"/function headingmarker:set {x:-250, y:64, z:450, color:2}","color":"aqua","clickEvent":{"action":"suggest_command","value":"/function headingmarker:set {x:-250, y:64, z:450, color:2}"},"hoverEvent":{"action":"show_text","contents":"Click to edit and run"}}]

playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.5 1


