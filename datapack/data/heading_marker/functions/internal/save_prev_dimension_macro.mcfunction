# Internal: Save Previous Dimension Markers Macro
# Macro that receives UUID, saves markers for PREVIOUS dimension to UUID-keyed storage
# Used during dimension changes

# Determine dimension string (overworld, nether, or end) based on PREVIOUS dimension
execute if score @s hm.prev_dim matches 0 run data modify storage heading_marker:temp dim set value "overworld"
execute if score @s hm.prev_dim matches -1 run data modify storage heading_marker:temp dim set value "nether"
execute if score @s hm.prev_dim matches 1 run data modify storage heading_marker:temp dim set value "end"

# Now save with UUID and dimension
function heading_marker:internal/save_dim_with_uuid with storage heading_marker:temp
