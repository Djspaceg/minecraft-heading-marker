# Internal: Save Dimension Markers Macro
# Macro that receives UUID, saves markers for current dimension to UUID-keyed storage

# Determine dimension string (overworld, nether, or end) based on CURRENT dimension
execute if score @s hm.dimension matches 0 run data modify storage headingmarker:temp dim set value "overworld"
execute if score @s hm.dimension matches -1 run data modify storage headingmarker:temp dim set value "nether"
execute if score @s hm.dimension matches 1 run data modify storage headingmarker:temp dim set value "end"

# Now save with UUID and dimension
function headingmarker:internal/save_dim_with_uuid with storage headingmarker:temp


