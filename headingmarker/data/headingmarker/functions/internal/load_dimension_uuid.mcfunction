# Internal: Load Current Dimension Markers with UUID
# Extracts player UUID and calls macro to load current dimension's markers

# Extract UUID and call macro
data modify storage headingmarker:temp uuid set from entity @s UUID
function headingmarker:internal/load_dimension_macro with storage headingmarker:temp


