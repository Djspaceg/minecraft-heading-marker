# Internal: Save Markers with ID and Dimension
# Macro that saves current markers to storage with ID and dimension keys
# Storage: headingmarker:players.$(id).$(dim).red/blue/green/yellow/purple

# Save red marker
$execute if score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).red.active set value 1b
$execute unless score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).red.active set value 0b
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).$(dim).red.x int 1 run scoreboard players get @s hm.red.x
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).$(dim).red.y int 1 run scoreboard players get @s hm.red.y
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).$(dim).red.z int 1 run scoreboard players get @s hm.red.z

# Save blue marker
$execute if score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).blue.active set value 1b
$execute unless score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).blue.active set value 0b
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).$(dim).blue.x int 1 run scoreboard players get @s hm.blue.x
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).$(dim).blue.y int 1 run scoreboard players get @s hm.blue.y
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).$(dim).blue.z int 1 run scoreboard players get @s hm.blue.z

# Save green marker
$execute if score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).green.active set value 1b
$execute unless score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).green.active set value 0b
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).$(dim).green.x int 1 run scoreboard players get @s hm.green.x
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).$(dim).green.y int 1 run scoreboard players get @s hm.green.y
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).$(dim).green.z int 1 run scoreboard players get @s hm.green.z

# Save yellow marker
$execute if score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).yellow.active set value 1b
$execute unless score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(id).$(dim).yellow.active set value 0b
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(id).$(dim).yellow.x int 1 run scoreboard players get @s hm.yellow.x
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(id).$(dim).yellow.y int 1 run scoreboard players get @s hm.yellow.y
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).yellow.z int 1 run scoreboard players get @s hm.yellow.z

# Save purple marker
$execute if score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).purple.active set value 1b
$execute unless score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).purple.active set value 0b
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.x int 1 run scoreboard players get @s hm.purple.x
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.y int 1 run scoreboard players get @s hm.purple.y
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.z int 1 run scoreboard players get @s hm.purple.z


