# Heading Marker - Set Marker at Coordinates
# Usage: First set the coordinates using /scoreboard, then run this function
# Example:
#   /scoreboard players set @s hm.x 1000
#   /scoreboard players set @s hm.y 64
#   /scoreboard players set @s hm.z -500
#   /function heading_marker:set_marker_at

# Check if coordinates are set
execute unless score @s hm.x = @s hm.x run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Error: X coordinate not set!","color":"red"}]
execute unless score @s hm.x = @s hm.x run return 0

# Activate marker for this player
scoreboard players set @s hm.active 1

# Set default color (0 = red, 1 = blue, 2 = green, 3 = yellow, 4 = purple)
scoreboard players set @s hm.color 0

# Display confirmation
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Waypoint marker set!","color":"green"}]
tellraw @s ["",{"text":"Coordinates: ","color":"gray"},{"score":{"name":"@s","objective":"hm.x"},"color":"aqua"},{"text":" ","color":"gray"},{"score":{"name":"@s","objective":"hm.y"},"color":"aqua"},{"text":" ","color":"gray"},{"score":{"name":"@s","objective":"hm.z"},"color":"aqua"}]
tellraw @s ["",{"text":"The marker will appear on your HUD!","color":"yellow"}]

# Play sound
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5
