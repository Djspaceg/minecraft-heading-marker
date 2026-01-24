# Heading Marker - Tick Function
# Runs every tick to update HUD markers

# For each player with an active marker, display the HUD
execute as @a[scores={hm.active=1}] run function heading_marker:display_hud
