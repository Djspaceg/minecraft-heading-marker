# Internal: Assign unique Player ID
# Used for storage mapping

# Initialize global counter if missing (safety check)
execute unless score #global hm.next_uid matches 1.. run scoreboard players set #global hm.next_uid 0

# Increment global ID counter
scoreboard players add #global hm.next_uid 1

# Assign to player
scoreboard players operation @s hm.uid = #global hm.next_uid

# Claim the associated storage slot (clears any stale data)
execute store result storage headingmarker:temp id int 1 run scoreboard players get @s hm.uid
function headingmarker:internal/verify_slot_macro with storage headingmarker:temp
