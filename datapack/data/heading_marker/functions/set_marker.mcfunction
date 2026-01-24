# Heading Marker - Set Marker Command
# Usage: /function heading_marker:set_marker
# This creates a waypoint marker at your current location

# Store current coordinates
execute store result score @s hm.x run data get entity @s Pos[0]
execute store result score @s hm.y run data get entity @s Pos[1]
execute store result score @s hm.z run data get entity @s Pos[2]

# Activate marker for this player
scoreboard players set @s hm.active 1

# Set default color (0 = red, 1 = blue, 2 = green, 3 = yellow, 4 = purple)
scoreboard players set @s hm.color 0

# Display confirmation
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Waypoint marker set at your location!","color":"green"}]
tellraw @s ["",{"text":"Coordinates: ","color":"gray"},{"score":{"name":"@s","objective":"hm.x"},"color":"aqua"},{"text":" ","color":"gray"},{"score":{"name":"@s","objective":"hm.y"},"color":"aqua"},{"text":" ","color":"gray"},{"score":{"name":"@s","objective":"hm.z"},"color":"aqua"}]
tellraw @s ["",{"text":"Use ","color":"yellow"},{"text":"/function heading_marker:clear_marker","color":"aqua"},{"text":" to remove it.","color":"yellow"}]

# Play sound
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5
