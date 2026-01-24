# Internal: Handle Dimension Change
# Called when a player changes dimensions
# Saves markers from old dimension, loads markers from new dimension

# Save markers from previous dimension (if we have prev_dim set)
execute if score @s hm.prev_dim matches -2147483648..2147483647 run function headingmarker:internal/save_dimension_uuid

# Update previous dimension tracker
scoreboard players operation @s hm.prev_dim = @s hm.dimension

# Load markers for new dimension
function headingmarker:internal/load_dimension_uuid

# Notify player
execute if score @s hm.dimension matches 0 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Switched to Overworld markers","color":"yellow"}]
execute if score @s hm.dimension matches -1 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Switched to Nether markers","color":"red"}]
execute if score @s hm.dimension matches 1 run tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"Switched to End markers","color":"light_purple"}]


