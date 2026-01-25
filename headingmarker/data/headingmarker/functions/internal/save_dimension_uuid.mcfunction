# Internal: Save Current Dimension Markers with ID
# Extracts player ID and calls macro to save PREVIOUS dimension's markers (during dimension change)

#  Extract ID and call macro
execute store result storage headingmarker:temp id int 1 run scoreboard players get @s hm.uid
function headingmarker:internal/save_prev_dimension_macro with storage headingmarker:temp


