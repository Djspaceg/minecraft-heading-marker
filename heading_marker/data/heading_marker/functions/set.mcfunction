# Heading Marker - Set Marker Command (Macro Version)
# Usage with macros (Minecraft 1.20.2+):
#   /function heading_marker:set {x:1000, y:64, z:-500}     # 3D mode (recommended)
#
# For other modes use:
#   /function heading_marker:set_2d {x:1000, z:-500}                    # 2D mode (y defaults to 64)
#   /function heading_marker:set_2d_color {x:1000, z:-500, color:0}     # 2D with color
#   /function heading_marker:set_3d_color {x:1000, y:64, z:-500, color:2}  # 3D with color
#
# Colors: 0=red, 1=blue, 2=green, 3=yellow, 4=purple
# Color -1 or omitted means auto-cycle to next available

# Store macro parameters to scoreboards
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.y $(y)
$scoreboard players set @s hm.input.z $(z)

# Auto-cycle color (set to -1 to trigger auto-select)
scoreboard players set @s hm.input.color -1

# Process the marker
function heading_marker:internal/set_marker_3d
