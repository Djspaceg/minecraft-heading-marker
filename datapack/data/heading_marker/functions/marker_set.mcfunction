# Heading Marker - Set Marker Command
# Usage: User sets coordinates via scoreboard, then calls this
# Syntax support:
#   /marker_set x y       - Sets marker at x,y (y=64 default for 2D)
#   /marker_set x y color - Sets marker at x,y with specific color
#   /marker_set x y z     - Sets marker at x,y,z
#   /marker_set x y z color - Sets marker at x,y,z with specific color
#
# Before calling this, set:
#   /scoreboard players set @s hm.input.x <value>
#   /scoreboard players set @s hm.input.y <value>  (optional, defaults to 64)
#   /scoreboard players set @s hm.input.z <value>  (optional for 2D mode)
#   /scoreboard players set @s hm.input.color <value>  (optional, auto-cycles if not set)

# Check if X coordinate is set (required)
execute unless score @s hm.input.x matches -2147483648.. run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Error: X coordinate not set!","color":"red"}]
execute unless score @s hm.input.x matches -2147483648.. run tellraw @s ["",{"text":"Use: /scoreboard players set @s hm.input.x <value>","color":"yellow"}]
execute unless score @s hm.input.x matches -2147483648.. run return 0

# Default Y to 64 if not set
execute unless score @s hm.input.y matches -2147483648.. run scoreboard players set @s hm.input.y 64

# Check if Z is set - if not, this is 2D mode (x and y are actually x and z)
execute unless score @s hm.input.z matches -2147483648.. run function heading_marker:internal/set_marker_2d
execute if score @s hm.input.z matches -2147483648.. run function heading_marker:internal/set_marker_3d
