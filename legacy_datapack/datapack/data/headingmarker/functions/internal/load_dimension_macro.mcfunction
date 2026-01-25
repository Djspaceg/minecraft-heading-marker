# Internal: Load Dimension Markers Macro
# Macro that receives UUID, loads markers for current dimension from UUID-keyed storage

# Determine dimension string (overworld, nether, or end)
execute if score @s hm.dimension matches 0 run data modify storage headingmarker:temp dim set value "overworld"
execute if score @s hm.dimension matches -1 run data modify storage headingmarker:temp dim set value "nether"
execute if score @s hm.dimension matches 1 run data modify storage headingmarker:temp dim set value "end"

# Now load with UUID and dimension
function headingmarker:internal/load_dim_with_uuid with storage headingmarker:temp


