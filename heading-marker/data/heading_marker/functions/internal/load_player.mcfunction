# Internal: Load Player Markers
# Load this player's markers from storage

# Load red marker
execute if data storage heading_marker:save red.active run scoreboard players set @s hm.red.active 1
execute if data storage heading_marker:save red.x store result score @s hm.red.x run data get storage heading_marker:save red.x
execute if data storage heading_marker:save red.y store result score @s hm.red.y run data get storage heading_marker:save red.y
execute if data storage heading_marker:save red.z store result score @s hm.red.z run data get storage heading_marker:save red.z

# Load blue marker
execute if data storage heading_marker:save blue.active run scoreboard players set @s hm.blue.active 1
execute if data storage heading_marker:save blue.x store result score @s hm.blue.x run data get storage heading_marker:save blue.x
execute if data storage heading_marker:save blue.y store result score @s hm.blue.y run data get storage heading_marker:save blue.y
execute if data storage heading_marker:save blue.z store result score @s hm.blue.z run data get storage heading_marker:save blue.z

# Load green marker
execute if data storage heading_marker:save green.active run scoreboard players set @s hm.green.active 1
execute if data storage heading_marker:save green.x store result score @s hm.green.x run data get storage heading_marker:save green.x
execute if data storage heading_marker:save green.y store result score @s hm.green.y run data get storage heading_marker:save green.y
execute if data storage heading_marker:save green.z store result score @s hm.green.z run data get storage heading_marker:save green.z

# Load yellow marker
execute if data storage heading_marker:save yellow.active run scoreboard players set @s hm.yellow.active 1
execute if data storage heading_marker:save yellow.x store result score @s hm.yellow.x run data get storage heading_marker:save yellow.x
execute if data storage heading_marker:save yellow.y store result score @s hm.yellow.y run data get storage heading_marker:save yellow.y
execute if data storage heading_marker:save yellow.z store result score @s hm.yellow.z run data get storage heading_marker:save yellow.z

# Load purple marker
execute if data storage heading_marker:save purple.active run scoreboard players set @s hm.purple.active 1
execute if data storage heading_marker:save purple.x store result score @s hm.purple.x run data get storage heading_marker:save purple.x
execute if data storage heading_marker:save purple.y store result score @s hm.purple.y run data get storage heading_marker:save purple.y
execute if data storage heading_marker:save purple.z store result score @s hm.purple.z run data get storage heading_marker:save purple.z
