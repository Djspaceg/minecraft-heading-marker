# Internal: Load Current Dimension Markers with ID
# Extracts player ID and calls macro to load current dimension's markers

# Extract ID and call macro
execute store result storage headingmarker:temp id int 1 run scoreboard players get @s hm.uid
function headingmarker:internal/load_dimension_macro with storage headingmarker:temp


