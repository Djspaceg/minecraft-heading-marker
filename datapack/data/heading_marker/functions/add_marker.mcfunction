# Heading Marker - Add Marker Command
# Usage: Place a lodestone, then right-click it with a compass
# Alternative: Use /function heading_marker:add_marker_coordinates for coordinate-based markers

# This function helps players create heading markers
# For now, we'll use lodestone compass mechanics which Minecraft provides natively

# Display usage instructions
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"To add a heading marker:","color":"yellow"}]
tellraw @s ["",{"text":"1. ","color":"gray"},{"text":"Place a Lodestone","color":"aqua"},{"text":" at your desired waypoint location","color":"yellow"}]
tellraw @s ["",{"text":"2. ","color":"gray"},{"text":"Hold a Compass","color":"aqua"},{"text":" and right-click the Lodestone","color":"yellow"}]
tellraw @s ["",{"text":"3. ","color":"gray"},{"text":"Your compass will now point to that location!","color":"green"}]
tellraw @s ["",{"text":"","color":"gray"},{"text":"Tip: ","color":"gold","bold":true},{"text":"You can create multiple lodestone compasses for different waypoints.","color":"yellow"}]

# Play a sound effect
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5
