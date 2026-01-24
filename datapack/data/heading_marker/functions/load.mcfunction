# Heading Marker - Initialization
# This function runs when the data pack loads

# Create scoreboards for tracking markers
scoreboard objectives add hm.marker dummy "Heading Marker"
scoreboard objectives add hm.marker.x dummy "Marker X Coordinate"
scoreboard objectives add hm.marker.y dummy "Marker Y Coordinate"
scoreboard objectives add hm.marker.z dummy "Marker Z Coordinate"

# Display welcome message
tellraw @a ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Data pack loaded! Use ","color":"yellow"},{"text":"/function heading_marker:add_marker","color":"aqua"},{"text":" and ","color":"yellow"},{"text":"/function heading_marker:remove_marker","color":"aqua"},{"text":" to manage your waypoints.","color":"yellow"}]
