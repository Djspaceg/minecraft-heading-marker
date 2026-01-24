# Internal: Save Current Dimension Markers with UUID
# Extracts player UUID and calls macro to save PREVIOUS dimension's markers (during dimension change)

#  Extract UUID and call macro
data modify storage heading_marker:temp uuid set from entity @s UUID
function heading_marker:internal/save_prev_dimension_macro with storage heading_marker:temp
