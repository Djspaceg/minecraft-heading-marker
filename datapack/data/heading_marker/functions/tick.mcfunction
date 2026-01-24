# Heading Marker - Tick Function
# Runs every tick to update HUD markers

# Detect players who just joined and haven't loaded their markers yet
# This handles the case where players join after the data pack was loaded
execute as @a unless score @s hm.loaded matches 1 run function heading_marker:internal/load_player_uuid
execute as @a unless score @s hm.loaded matches 1 run scoreboard players set @s hm.loaded 1

# Track current dimension for each player (overworld=0, nether=-1, end=1)
execute as @a[predicate=!heading_marker:in_overworld,predicate=!heading_marker:in_nether,predicate=!heading_marker:in_end] in minecraft:overworld run scoreboard players set @s hm.dimension 0
execute as @a[predicate=heading_marker:in_overworld] run scoreboard players set @s hm.dimension 0
execute as @a[predicate=heading_marker:in_nether] run scoreboard players set @s hm.dimension -1
execute as @a[predicate=heading_marker:in_end] run scoreboard players set @s hm.dimension 1

# Detect dimension changes and handle them
execute as @a unless score @s hm.prev_dim = @s hm.dimension run function heading_marker:internal/handle_dimension_change

# For each player, display all active markers
execute as @a run function heading_marker:display_all_markers
