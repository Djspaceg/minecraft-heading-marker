# Internal: Load Player Markers with UUID
# Uses UUID-based storage for proper multiplayer persistence
# Loads markers for current dimension

# Store UUID and call macro
data modify storage headingmarker:temp uuid set from entity @s UUID
function headingmarker:internal/load_dimension_macro with storage headingmarker:temp


