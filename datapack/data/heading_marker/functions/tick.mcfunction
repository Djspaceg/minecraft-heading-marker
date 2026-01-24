# Heading Marker - Tick Function
# Runs every tick to update HUD markers

# Detect players who just joined and haven't loaded their markers yet
# This handles the case where players join after the data pack was loaded
execute as @a unless score @s hm.loaded matches 1 run function heading_marker:internal/load_player
execute as @a unless score @s hm.loaded matches 1 run scoreboard players set @s hm.loaded 1

# Reset join detection for players who left (so they reload on next join)
execute as @a if score @s hm.joined matches 1.. run scoreboard players set @s hm.joined 0

# For each player, display all active markers
execute as @a run function heading_marker:display_all_markers
