# Heading Marker - Set Marker Command (Macro Version)
# Usage with macros (Minecraft 1.20.2+):
#   /function heading_marker:set {x:1000, z:-500}                    # 2D mode (y defaults to 64)
#   /function heading_marker:set {x:1000, z:-500, color:0}           # 2D with color
#   /function heading_marker:set {x:1000, y:64, z:-500}              # 3D mode
#   /function heading_marker:set {x:1000, y:64, z:-500, color:2}     # 3D with color
#
# Colors: 0=red, 1=blue, 2=green, 3=yellow, 4=purple
# Color -1 means auto-cycle to next available

# Store macro parameters to scoreboards (with defaults)
$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.y $(y)
$scoreboard players set @s hm.input.z $(z)
$scoreboard players set @s hm.input.color $(color)

# Default y to 64 if not in valid range (handling optional parameter)
execute unless score @s hm.input.y matches -2048..2048 run scoreboard players set @s hm.input.y 64

# Process the marker (no need for 2D/3D split anymore - all params are set)
function heading_marker:internal/set_marker_3d
