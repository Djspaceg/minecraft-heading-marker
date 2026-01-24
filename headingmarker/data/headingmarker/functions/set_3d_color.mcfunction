# Heading Marker - Set Marker (3D mode with color)
# Usage: /function headingmarker:set_3d_color {x:1000, y:64, z:-500, color:2}

# Store macro parameters to scoreboards
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.y $(y)
$scoreboard players set @s hm.input.z $(z)
$scoreboard players set @s hm.input.color $(color)

# Process the marker
function headingmarker:internal/set_marker_3d


