# Apply green color (RGB 0, 255, 0)
waypoint modify @s color 65280

# Set unlimited range
attribute @s minecraft:waypoint.transmission_range base set 999999
attribute @s waypoint_transmission_range base set 999999
attribute @s minecraft:waypoint_transmission_range base set 999999

# Remove temporary tag
tag @s remove hm.waypoint.new
