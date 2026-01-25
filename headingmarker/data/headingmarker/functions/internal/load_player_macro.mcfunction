# Internal: Load Player Markers Macro
# Macro that receives ID and loads markers from ID-keyed storage

# Load red marker
$execute if data storage headingmarker:players.$(id).red{active:1b} run scoreboard players set @s hm.red.active 1
$execute unless data storage headingmarker:players.$(id).red{active:1b} run scoreboard players set @s hm.red.active 0
$execute if data storage headingmarker:players.$(id).red.x store result score @s hm.red.x run data get storage headingmarker:players.$(id).red.x
$execute if data storage headingmarker:players.$(id).red.y store result score @s hm.red.y run data get storage headingmarker:players.$(id).red.y
$execute if data storage headingmarker:players.$(id).red.z store result score @s hm.red.z run data get storage headingmarker:players.$(id).red.z

# Load blue marker
$execute if data storage headingmarker:players.$(id).blue{active:1b} run scoreboard players set @s hm.blue.active 1
$execute unless data storage headingmarker:players.$(id).blue{active:1b} run scoreboard players set @s hm.blue.active 0
$execute if data storage headingmarker:players.$(id).blue.x store result score @s hm.blue.x run data get storage headingmarker:players.$(id).blue.x
$execute if data storage headingmarker:players.$(id).blue.y store result score @s hm.blue.y run data get storage headingmarker:players.$(id).blue.y
$execute if data storage headingmarker:players.$(id).blue.z store result score @s hm.blue.z run data get storage headingmarker:players.$(id).blue.z

# Load green marker
$execute if data storage headingmarker:players.$(id).green{active:1b} run scoreboard players set @s hm.green.active 1
$execute unless data storage headingmarker:players.$(id).green{active:1b} run scoreboard players set @s hm.green.active 0
$execute if data storage headingmarker:players.$(id).green.x store result score @s hm.green.x run data get storage headingmarker:players.$(id).green.x
$execute if data storage headingmarker:players.$(id).green.y store result score @s hm.green.y run data get storage headingmarker:players.$(id).green.y
$execute if data storage headingmarker:players.$(id).green.z store result score @s hm.green.z run data get storage headingmarker:players.$(id).green.z

# Load yellow marker
$execute if data storage headingmarker:players.$(id).yellow{active:1b} run scoreboard players set @s hm.yellow.active 1
$execute unless data storage headingmarker:players.$(id).yellow{active:1b} run scoreboard players set @s hm.yellow.active 0
$execute if data storage headingmarker:players.$(id).yellow.x store result score @s hm.yellow.x run data get storage headingmarker:players.$(id).yellow.x
$execute if data storage headingmarker:players.$(id).yellow.y store result score @s hm.yellow.y run data get storage headingmarker:players.$(id).yellow.y
$execute if data storage headingmarker:players.$(id).yellow.z store result score @s hm.yellow.z run data get storage headingmarker:players.$(id).yellow.z

# Load purple marker
$execute if data storage headingmarker:players.$(uuid).purple{active:1b} run scoreboard players set @s hm.purple.active 1
$execute unless data storage headingmarker:players.$(uuid).purple{active:1b} run scoreboard players set @s hm.purple.active 0
$execute if data storage headingmarker:players.$(uuid).purple.x store result score @s hm.purple.x run data get storage headingmarker:players.$(uuid).purple.x
$execute if data storage headingmarker:players.$(uuid).purple.y store result score @s hm.purple.y run data get storage headingmarker:players.$(uuid).purple.y
$execute if data storage headingmarker:players.$(uuid).purple.z store result score @s hm.purple.z run data get storage headingmarker:players.$(uuid).purple.z


