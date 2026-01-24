# Heading Marker - Initialization
# This function runs when the data pack loads

# Create scoreboards for tracking markers (5 markers per player, one per color)
# Red marker (0)
scoreboard objectives add hm.red.x dummy "Red Marker X"
scoreboard objectives add hm.red.y dummy "Red Marker Y"
scoreboard objectives add hm.red.z dummy "Red Marker Z"
scoreboard objectives add hm.red.active dummy "Red Active"
scoreboard objectives add hm.red.dist dummy "Red Distance"

# Blue marker (1)
scoreboard objectives add hm.blue.x dummy "Blue Marker X"
scoreboard objectives add hm.blue.y dummy "Blue Marker Y"
scoreboard objectives add hm.blue.z dummy "Blue Marker Z"
scoreboard objectives add hm.blue.active dummy "Blue Active"
scoreboard objectives add hm.blue.dist dummy "Blue Distance"

# Green marker (2)
scoreboard objectives add hm.green.x dummy "Green Marker X"
scoreboard objectives add hm.green.y dummy "Green Marker Y"
scoreboard objectives add hm.green.z dummy "Green Marker Z"
scoreboard objectives add hm.green.active dummy "Green Active"
scoreboard objectives add hm.green.dist dummy "Green Distance"

# Yellow marker (3)
scoreboard objectives add hm.yellow.x dummy "Yellow Marker X"
scoreboard objectives add hm.yellow.y dummy "Yellow Marker Y"
scoreboard objectives add hm.yellow.z dummy "Yellow Marker Z"
scoreboard objectives add hm.yellow.active dummy "Yellow Active"
scoreboard objectives add hm.yellow.dist dummy "Yellow Distance"

# Purple marker (4)
scoreboard objectives add hm.purple.x dummy "Purple Marker X"
scoreboard objectives add hm.purple.y dummy "Purple Marker Y"
scoreboard objectives add hm.purple.z dummy "Purple Marker Z"
scoreboard objectives add hm.purple.active dummy "Purple Active"
scoreboard objectives add hm.purple.dist dummy "Purple Distance"

# Calculation variables
scoreboard objectives add hm.dx dummy "Delta X"
scoreboard objectives add hm.dz dummy "Delta Z"
scoreboard objectives add hm.dist dummy "Distance"
scoreboard objectives add hm.temp dummy "Temp Variable"

# Command input variables
scoreboard objectives add hm.input.x dummy "Input X"
scoreboard objectives add hm.input.y dummy "Input Y"
scoreboard objectives add hm.input.z dummy "Input Z"
scoreboard objectives add hm.input.color dummy "Input Color"

# Next color tracking for auto-cycling
scoreboard objectives add hm.nextcolor dummy "Next Color"

# Display welcome message
tellraw @a ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Data pack loaded! Use ","color":"yellow"},{"text":"/function heading_marker:marker_set","color":"aqua"},{"text":" to add waypoint markers.","color":"yellow"}]

# Load saved markers from storage
function heading_marker:load_markers
