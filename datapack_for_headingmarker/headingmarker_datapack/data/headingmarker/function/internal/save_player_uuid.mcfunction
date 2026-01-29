# Internal: Save Player Markers with ID
# Uses ID-based storage for proper multiplayer persistence
# Saves markers for current dimension

# Store ID and call macro
execute store result storage headingmarker:temp id int 1 run scoreboard players get @s hm.uid
function headingmarker:internal/save_dimension_macro with storage headingmarker:temp


