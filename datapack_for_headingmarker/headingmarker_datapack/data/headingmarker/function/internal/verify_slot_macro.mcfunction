# Internal: Validate and Claim Storage Slot
# Input: $(id)

# This runs ONLY when a player is assigned a NEW ID.
# Therefore, any existing data at this ID is stale (orphan) and must be removed.
# This prevents data collisions if the global counter resets or wraps.

# Check if data exists (Debug info)
$execute if data storage headingmarker:players.$(id) run tellraw @s ["",{"text":"[Heading Marker] ","color":"gray"},{"text":"Claiming ID $(id) (clearing old data)","color":"dark_gray"}]

# Wipe any existing data for this ID
$data remove storage headingmarker:players.$(id)

# Set owner (helpful for future debugging)
$data modify storage headingmarker:players.$(id).owner set from entity @s UUID