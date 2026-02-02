# Apply waypoint color (red for single marker)
waypoint modify @s color 16711680

# Set transmission range (how far it can be detected from)
attribute @s minecraft:waypoint.transmission_range base set 999999
attribute @s waypoint_transmission_range base set 999999
attribute @s minecraft:waypoint_transmission_range base set 999999

# Fallback: if attribute is not registered, ensure the Attributes NBT entries exist so the client can read them
data modify entity @s Attributes append value {Name:"minecraft:waypoint.transmission_range",Base:999999}
data modify entity @s Attributes append value {Name:"waypoint_transmission_range",Base:999999}
data modify entity @s Attributes append value {Name:"minecraft:waypoint_transmission_range",Base:999999}

# Remove the "new" tag so it won't be configured again
tag @s remove hm.waypoint.new
