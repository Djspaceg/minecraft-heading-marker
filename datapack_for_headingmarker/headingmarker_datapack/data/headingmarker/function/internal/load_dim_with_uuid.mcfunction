# Internal: Load Markers with ID and Dimension
# Macro that loads markers from storage with ID and dimension keys
# Storage: headingmarker:players.$(id).$(dim).red/blue/green/yellow/purple

# Load red marker
$execute if data storage headingmarker:players.$(id).$(dim).red{active:1b} run scoreboard players set @s hm.red.active 1
$execute unless data storage headingmarker:players.$(id).$(dim).red{active:1b} run scoreboard players set @s hm.red.active 0
$execute if data storage headingmarker:players.$(id).$(dim).red.x store result score @s hm.red.x run data get storage headingmarker:players.$(id).$(dim).red.x
$execute if data storage headingmarker:players.$(id).$(dim).red.y store result score @s hm.red.y run data get storage headingmarker:players.$(id).$(dim).red.y
$execute if data storage headingmarker:players.$(id).$(dim).red.z store result score @s hm.red.z run data get storage headingmarker:players.$(id).$(dim).red.z

# Load blue marker
$execute if data storage headingmarker:players.$(id).$(dim).blue{active:1b} run scoreboard players set @s hm.blue.active 1
$execute unless data storage headingmarker:players.$(id).$(dim).blue{active:1b} run scoreboard players set @s hm.blue.active 0
$execute if data storage headingmarker:players.$(id).$(dim).blue.x store result score @s hm.blue.x run data get storage headingmarker:players.$(id).$(dim).blue.x
$execute if data storage headingmarker:players.$(id).$(dim).blue.y store result score @s hm.blue.y run data get storage headingmarker:players.$(id).$(dim).blue.y
$execute if data storage headingmarker:players.$(id).$(dim).blue.z store result score @s hm.blue.z run data get storage headingmarker:players.$(id).$(dim).blue.z

# Load green marker
$execute if data storage headingmarker:players.$(id).$(dim).green{active:1b} run scoreboard players set @s hm.green.active 1
$execute unless data storage headingmarker:players.$(id).$(dim).green{active:1b} run scoreboard players set @s hm.green.active 0
$execute if data storage headingmarker:players.$(id).$(dim).green.x store result score @s hm.green.x run data get storage headingmarker:players.$(id).$(dim).green.x
$execute if data storage headingmarker:players.$(id).$(dim).green.y store result score @s hm.green.y run data get storage headingmarker:players.$(id).$(dim).green.y
$execute if data storage headingmarker:players.$(id).$(dim).green.z store result score @s hm.green.z run data get storage headingmarker:players.$(id).$(dim).green.z

# Load yellow marker
$execute if data storage headingmarker:players.$(id).$(dim).yellow{active:1b} run scoreboard players set @s hm.yellow.active 1
$execute unless data storage headingmarker:players.$(id).$(dim).yellow{active:1b} run scoreboard players set @s hm.yellow.active 0
$execute if data storage headingmarker:players.$(id).$(dim).yellow.x store result score @s hm.yellow.x run data get storage headingmarker:players.$(id).$(dim).yellow.x
$execute if data storage headingmarker:players.$(id).$(dim).yellow.y store result score @s hm.yellow.y run data get storage headingmarker:players.$(id).$(dim).yellow.y
$execute if data storage headingmarker:players.$(uuid).$(dim).yellow.z store result score @s hm.yellow.z run data get storage headingmarker:players.$(uuid).$(dim).yellow.z

# Load purple marker
$execute if data storage headingmarker:players.$(uuid).$(dim).purple{active:1b} run scoreboard players set @s hm.purple.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).purple{active:1b} run scoreboard players set @s hm.purple.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.x store result score @s hm.purple.x run data get storage headingmarker:players.$(uuid).$(dim).purple.x
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.y store result score @s hm.purple.y run data get storage headingmarker:players.$(uuid).$(dim).purple.y
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.z store result score @s hm.purple.z run data get storage headingmarker:players.$(uuid).$(dim).purple.z


