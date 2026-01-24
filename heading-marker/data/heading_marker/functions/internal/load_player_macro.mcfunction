# Internal: Load Player Markers Macro
# Macro that receives UUID and loads markers from UUID-keyed storage

# Load red marker
$execute if data storage heading_marker:players.$(uuid).red{active:1b} run scoreboard players set @s hm.red.active 1
$execute unless data storage heading_marker:players.$(uuid).red{active:1b} run scoreboard players set @s hm.red.active 0
$execute if data storage heading_marker:players.$(uuid).red.x store result score @s hm.red.x run data get storage heading_marker:players.$(uuid).red.x
$execute if data storage heading_marker:players.$(uuid).red.y store result score @s hm.red.y run data get storage heading_marker:players.$(uuid).red.y
$execute if data storage heading_marker:players.$(uuid).red.z store result score @s hm.red.z run data get storage heading_marker:players.$(uuid).red.z

# Load blue marker
$execute if data storage heading_marker:players.$(uuid).blue{active:1b} run scoreboard players set @s hm.blue.active 1
$execute unless data storage heading_marker:players.$(uuid).blue{active:1b} run scoreboard players set @s hm.blue.active 0
$execute if data storage heading_marker:players.$(uuid).blue.x store result score @s hm.blue.x run data get storage heading_marker:players.$(uuid).blue.x
$execute if data storage heading_marker:players.$(uuid).blue.y store result score @s hm.blue.y run data get storage heading_marker:players.$(uuid).blue.y
$execute if data storage heading_marker:players.$(uuid).blue.z store result score @s hm.blue.z run data get storage heading_marker:players.$(uuid).blue.z

# Load green marker
$execute if data storage heading_marker:players.$(uuid).green{active:1b} run scoreboard players set @s hm.green.active 1
$execute unless data storage heading_marker:players.$(uuid).green{active:1b} run scoreboard players set @s hm.green.active 0
$execute if data storage heading_marker:players.$(uuid).green.x store result score @s hm.green.x run data get storage heading_marker:players.$(uuid).green.x
$execute if data storage heading_marker:players.$(uuid).green.y store result score @s hm.green.y run data get storage heading_marker:players.$(uuid).green.y
$execute if data storage heading_marker:players.$(uuid).green.z store result score @s hm.green.z run data get storage heading_marker:players.$(uuid).green.z

# Load yellow marker
$execute if data storage heading_marker:players.$(uuid).yellow{active:1b} run scoreboard players set @s hm.yellow.active 1
$execute unless data storage heading_marker:players.$(uuid).yellow{active:1b} run scoreboard players set @s hm.yellow.active 0
$execute if data storage heading_marker:players.$(uuid).yellow.x store result score @s hm.yellow.x run data get storage heading_marker:players.$(uuid).yellow.x
$execute if data storage heading_marker:players.$(uuid).yellow.y store result score @s hm.yellow.y run data get storage heading_marker:players.$(uuid).yellow.y
$execute if data storage heading_marker:players.$(uuid).yellow.z store result score @s hm.yellow.z run data get storage heading_marker:players.$(uuid).yellow.z

# Load purple marker
$execute if data storage heading_marker:players.$(uuid).purple{active:1b} run scoreboard players set @s hm.purple.active 1
$execute unless data storage heading_marker:players.$(uuid).purple{active:1b} run scoreboard players set @s hm.purple.active 0
$execute if data storage heading_marker:players.$(uuid).purple.x store result score @s hm.purple.x run data get storage heading_marker:players.$(uuid).purple.x
$execute if data storage heading_marker:players.$(uuid).purple.y store result score @s hm.purple.y run data get storage heading_marker:players.$(uuid).purple.y
$execute if data storage heading_marker:players.$(uuid).purple.z store result score @s hm.purple.z run data get storage heading_marker:players.$(uuid).purple.z
