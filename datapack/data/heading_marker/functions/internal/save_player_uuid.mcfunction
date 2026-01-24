# Internal: Save Player Markers with UUID
# Uses UUID-based storage for proper multiplayer persistence

# Store UUID and call macro
data modify storage heading_marker:temp uuid set from entity @s UUID
function heading_marker:internal/save_player_macro with storage heading_marker:temp
