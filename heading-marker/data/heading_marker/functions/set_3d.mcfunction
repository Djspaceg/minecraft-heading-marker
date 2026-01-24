# Heading Marker - Set Marker (3D mode)
# Usage: /function heading_marker:set_3d {x:1000, y:64, z:-500}

# Store macro parameters to scoreboards
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.y $(y)
$scoreboard players set @s hm.input.z $(z)

# Auto-cycle color (set to -1 to trigger auto-select)
scoreboard players set @s hm.input.color -1

# Process the marker
function heading_marker:internal/set_marker_3d
