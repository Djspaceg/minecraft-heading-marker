# Heading Marker - Set Marker (2D mode)
# Usage: /function heading_marker:set_2d {x:1000, z:-500}
# Y defaults to 64

# Store macro parameters to scoreboards
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.z $(z)
scoreboard players set @s hm.input.y 64

# Auto-cycle color (set to -1 to trigger auto-select)
scoreboard players set @s hm.input.color -1

# Process the marker
function heading_marker:internal/set_marker_3d
