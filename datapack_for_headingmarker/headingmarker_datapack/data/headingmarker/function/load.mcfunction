# Initialize scoreboard for tracking entity IDs
scoreboard objectives add hm.waypoint.id dummy
scoreboard objectives add hm.has.waypoint dummy

# Initialize input variables for commands
scoreboard objectives add hm.input.x dummy
scoreboard objectives add hm.input.y dummy
scoreboard objectives add hm.input.z dummy
scoreboard objectives add hm.waypoint.count dummy

# Player UUID tracking for entity tagging
scoreboard objectives add hm.uuid dummy

# Color-specific waypoint flags (1 = active, 0 = inactive)
scoreboard objectives add hm.red.active dummy
scoreboard objectives add hm.blue.active dummy
scoreboard objectives add hm.green.active dummy
scoreboard objectives add hm.yellow.active dummy
scoreboard objectives add hm.purple.active dummy

# Color input
scoreboard objectives add hm.input.color dummy

# Auto-cycling tracker
scoreboard objectives add hm.nextcolor dummy

# Announce successful load
tellraw @a {"text":"Heading Marker loaded! Use /function headingmarker:set to create a waypoint.","color":"green"}
tellraw @a {"text":"Waypoints will appear in your Locator Bar!","color":"gray","italic":true}

# Re-initialize existing markers (restore attributes/waypoint data)
function headingmarker:internal/reinit_markers
