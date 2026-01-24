# Internal: Set Marker 2D Mode
# When Z is not provided, Y becomes Z and Y defaults to 64
# hm.input.x = X coordinate
# hm.input.y = Z coordinate (what user provided as second arg)
# hm.input.z = not set

# Swap Y to temp, set actual Y to 64, set Z to what was in Y
scoreboard players operation @s hm.temp = @s hm.input.y
scoreboard players set @s hm.input.y 64
scoreboard players operation @s hm.input.z = @s hm.temp

# Now call the 3D handler
function heading_marker:internal/set_marker_3d
