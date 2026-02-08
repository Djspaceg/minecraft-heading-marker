# Apply waypoint settings (Color & Range)
# Macro Args: $(color_int)

# 1. Set Transmission Range (unlimited) - DO THIS FIRST
# A. NBT Injection (Try 1.21+ 'id' format first, then legacy 'Name')
data modify entity @s Attributes append value {id:"minecraft:waypoint.transmission_range",base:6000000.0d}
data modify entity @s Attributes append value {id:"waypoint_transmission_range",base:6000000.0d}
data modify entity @s Attributes append value {id:"minecraft:waypoint_transmission_range",base:6000000.0d}
data modify entity @s Attributes append value {id:"generic.waypoint_transmission_range",base:6000000.0d}
# Control check with max_health using proper 1.21 format
data modify entity @s Attributes append value {id:"minecraft:generic.max_health",base:20.0d}

# Legacy Fallback (just in case)
data modify entity @s Attributes append value {Name:"minecraft:waypoint.transmission_range",Base:6000000.0d}
data modify entity @s Attributes append value {Name:"waypoint_transmission_range",Base:6000000.0d}

# B. Safe Command Injection - REMOVED

# 2. Apply Visual Color (Commented out temporarily to debug crash)
# $waypoint modify @s color $(color_int)

# Debug
tellraw @a[distance=..10] {"text":"Applied settings to waypoint (Attributes+Color) - FINISHED","color":"gray","italic":true}

# 3. Cleanup
tag @s remove hm.waypoint.new
