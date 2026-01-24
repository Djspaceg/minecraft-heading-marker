# Internal: Save Player Markers with UUID
# Uses UUID-based storage for proper multiplayer persistence
# Saves markers for current dimension

# Store UUID and call macro
data modify storage heading_marker:temp uuid set from entity @s UUID
function heading_marker:internal/save_dimension_macro with storage heading_marker:temp
