# Heading Marker - Initialization
# This function runs when the data pack loads

# Create scoreboards for tracking markers
scoreboard objectives add hm.marker dummy "Heading Marker"
scoreboard objectives add hm.id dummy "Marker ID"
scoreboard objectives add hm.x dummy "Target X"
scoreboard objectives add hm.y dummy "Target Y"
scoreboard objectives add hm.z dummy "Target Z"
scoreboard objectives add hm.color dummy "Marker Color"
scoreboard objectives add hm.active dummy "Marker Active"
scoreboard objectives add hm.dx dummy "Delta X"
scoreboard objectives add hm.dz dummy "Delta Z"
scoreboard objectives add hm.dist dummy "Distance"
scoreboard objectives add hm.temp dummy "Temp Variable"

# Display welcome message
tellraw @a ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Data pack loaded! Use ","color":"yellow"},{"text":"/function heading_marker:set_marker","color":"aqua"},{"text":" to add waypoint markers to your HUD.","color":"yellow"}]
