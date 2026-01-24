# Internal: Save Player Markers
# Save this player's markers to storage

# Note: This implementation uses shared storage (not UUID-keyed)
# Works for single-player and small servers, but only saves the last player's state
# For true multiplayer persistence, would need UUID-keyed storage

# Save red marker (including clearing if inactive)
execute if score @s hm.red.active matches 1 run data modify storage heading_marker:save red.active set value 1b
execute unless score @s hm.red.active matches 1 run data modify storage heading_marker:save red.active set value 0b
execute if score @s hm.red.active matches 1 store result storage heading_marker:save red.x int 1 run scoreboard players get @s hm.red.x
execute if score @s hm.red.active matches 1 store result storage heading_marker:save red.y int 1 run scoreboard players get @s hm.red.y
execute if score @s hm.red.active matches 1 store result storage heading_marker:save red.z int 1 run scoreboard players get @s hm.red.z

# Save blue marker (including clearing if inactive)
execute if score @s hm.blue.active matches 1 run data modify storage heading_marker:save blue.active set value 1b
execute unless score @s hm.blue.active matches 1 run data modify storage heading_marker:save blue.active set value 0b
execute if score @s hm.blue.active matches 1 store result storage heading_marker:save blue.x int 1 run scoreboard players get @s hm.blue.x
execute if score @s hm.blue.active matches 1 store result storage heading_marker:save blue.y int 1 run scoreboard players get @s hm.blue.y
execute if score @s hm.blue.active matches 1 store result storage heading_marker:save blue.z int 1 run scoreboard players get @s hm.blue.z

# Save green marker (including clearing if inactive)
execute if score @s hm.green.active matches 1 run data modify storage heading_marker:save green.active set value 1b
execute unless score @s hm.green.active matches 1 run data modify storage heading_marker:save green.active set value 0b
execute if score @s hm.green.active matches 1 store result storage heading_marker:save green.x int 1 run scoreboard players get @s hm.green.x
execute if score @s hm.green.active matches 1 store result storage heading_marker:save green.y int 1 run scoreboard players get @s hm.green.y
execute if score @s hm.green.active matches 1 store result storage heading_marker:save green.z int 1 run scoreboard players get @s hm.green.z

# Save yellow marker (including clearing if inactive)
execute if score @s hm.yellow.active matches 1 run data modify storage heading_marker:save yellow.active set value 1b
execute unless score @s hm.yellow.active matches 1 run data modify storage heading_marker:save yellow.active set value 0b
execute if score @s hm.yellow.active matches 1 store result storage heading_marker:save yellow.x int 1 run scoreboard players get @s hm.yellow.x
execute if score @s hm.yellow.active matches 1 store result storage heading_marker:save yellow.y int 1 run scoreboard players get @s hm.yellow.y
execute if score @s hm.yellow.active matches 1 store result storage heading_marker:save yellow.z int 1 run scoreboard players get @s hm.yellow.z

# Save purple marker (including clearing if inactive)
execute if score @s hm.purple.active matches 1 run data modify storage heading_marker:save purple.active set value 1b
execute unless score @s hm.purple.active matches 1 run data modify storage heading_marker:save purple.active set value 0b
execute if score @s hm.purple.active matches 1 store result storage heading_marker:save purple.x int 1 run scoreboard players get @s hm.purple.x
execute if score @s hm.purple.active matches 1 store result storage heading_marker:save purple.y int 1 run scoreboard players get @s hm.purple.y
execute if score @s hm.purple.active matches 1 store result storage heading_marker:save purple.z int 1 run scoreboard players get @s hm.purple.z
