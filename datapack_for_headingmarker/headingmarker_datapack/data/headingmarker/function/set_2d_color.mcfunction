# Heading Marker - Set Marker (2D mode with color)
# Usage: /function headingmarker:set_2d_color {x:1000, z:-500, color:0}

# Store macro parameters to scoreboards
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.z $(z)
$scoreboard players set @s hm.input.color $(color)
scoreboard players set @s hm.input.y 64

# Process the marker
function headingmarker:internal/set_marker_3d


