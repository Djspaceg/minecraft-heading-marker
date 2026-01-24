# Internal: Load Markers with UUID and Dimension
# Macro that loads markers from storage with UUID and dimension keys
# Storage: headingmarker:players.$(uuid).$(dim).red/blue/green/yellow/purple

# Load red marker
$execute if data storage headingmarker:players.$(uuid).$(dim).red{active:1b} run scoreboard players set @s hm.red.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).red{active:1b} run scoreboard players set @s hm.red.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).red.x store result score @s hm.red.x run data get storage headingmarker:players.$(uuid).$(dim).red.x
$execute if data storage headingmarker:players.$(uuid).$(dim).red.y store result score @s hm.red.y run data get storage headingmarker:players.$(uuid).$(dim).red.y
$execute if data storage headingmarker:players.$(uuid).$(dim).red.z store result score @s hm.red.z run data get storage headingmarker:players.$(uuid).$(dim).red.z

# Load blue marker
$execute if data storage headingmarker:players.$(uuid).$(dim).blue{active:1b} run scoreboard players set @s hm.blue.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).blue{active:1b} run scoreboard players set @s hm.blue.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).blue.x store result score @s hm.blue.x run data get storage headingmarker:players.$(uuid).$(dim).blue.x
$execute if data storage headingmarker:players.$(uuid).$(dim).blue.y store result score @s hm.blue.y run data get storage headingmarker:players.$(uuid).$(dim).blue.y
$execute if data storage headingmarker:players.$(uuid).$(dim).blue.z store result score @s hm.blue.z run data get storage headingmarker:players.$(uuid).$(dim).blue.z

# Load green marker
$execute if data storage headingmarker:players.$(uuid).$(dim).green{active:1b} run scoreboard players set @s hm.green.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).green{active:1b} run scoreboard players set @s hm.green.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).green.x store result score @s hm.green.x run data get storage headingmarker:players.$(uuid).$(dim).green.x
$execute if data storage headingmarker:players.$(uuid).$(dim).green.y store result score @s hm.green.y run data get storage headingmarker:players.$(uuid).$(dim).green.y
$execute if data storage headingmarker:players.$(uuid).$(dim).green.z store result score @s hm.green.z run data get storage headingmarker:players.$(uuid).$(dim).green.z

# Load yellow marker
$execute if data storage headingmarker:players.$(uuid).$(dim).yellow{active:1b} run scoreboard players set @s hm.yellow.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).yellow{active:1b} run scoreboard players set @s hm.yellow.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).yellow.x store result score @s hm.yellow.x run data get storage headingmarker:players.$(uuid).$(dim).yellow.x
$execute if data storage headingmarker:players.$(uuid).$(dim).yellow.y store result score @s hm.yellow.y run data get storage headingmarker:players.$(uuid).$(dim).yellow.y
$execute if data storage headingmarker:players.$(uuid).$(dim).yellow.z store result score @s hm.yellow.z run data get storage headingmarker:players.$(uuid).$(dim).yellow.z

# Load purple marker
$execute if data storage headingmarker:players.$(uuid).$(dim).purple{active:1b} run scoreboard players set @s hm.purple.active 1
$execute unless data storage headingmarker:players.$(uuid).$(dim).purple{active:1b} run scoreboard players set @s hm.purple.active 0
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.x store result score @s hm.purple.x run data get storage headingmarker:players.$(uuid).$(dim).purple.x
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.y store result score @s hm.purple.y run data get storage headingmarker:players.$(uuid).$(dim).purple.y
$execute if data storage headingmarker:players.$(uuid).$(dim).purple.z store result score @s hm.purple.z run data get storage headingmarker:players.$(uuid).$(dim).purple.z


