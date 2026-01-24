# Heading Marker - Tick Function
# Runs every tick to update HUD markers

# Detect players who just joined and haven't loaded their markers yet
# This handles the case where players join after the data pack was loaded
execute as @a unless score @s hm.loaded matches 1 run function headingmarker:internal/load_player_uuid
execute as @a unless score @s hm.loaded matches 1 run scoreboard players set @s hm.loaded 1

# Track current dimension for each player (overworld=0, nether=-1, end=1)
execute as @a[predicate=!headingmarker:in_overworld,predicate=!headingmarker:in_nether,predicate=!headingmarker:in_end] in minecraft:overworld run scoreboard players set @s hm.dimension 0
execute as @a[predicate=headingmarker:in_overworld] run scoreboard players set @s hm.dimension 0
execute as @a[predicate=headingmarker:in_nether] run scoreboard players set @s hm.dimension -1
execute as @a[predicate=headingmarker:in_end] run scoreboard players set @s hm.dimension 1

# Detect dimension changes and handle them
execute as @a unless score @s hm.prev_dim = @s hm.dimension run function headingmarker:internal/handle_dimension_change

# For each player, display all active markers
execute as @a run function headingmarker:display_all_markers


