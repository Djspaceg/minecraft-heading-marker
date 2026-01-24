# Internal: Save Markers with UUID and Dimension
# Macro that saves current markers to storage with UUID and dimension keys
# Storage: headingmarker:players.$(uuid).$(dim).red/blue/green/yellow/purple

# Save red marker
$execute if score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).red.active set value 1b
$execute unless score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).red.active set value 0b
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).red.x int 1 run scoreboard players get @s hm.red.x
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).red.y int 1 run scoreboard players get @s hm.red.y
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).red.z int 1 run scoreboard players get @s hm.red.z

# Save blue marker
$execute if score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).blue.active set value 1b
$execute unless score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).blue.active set value 0b
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).blue.x int 1 run scoreboard players get @s hm.blue.x
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).blue.y int 1 run scoreboard players get @s hm.blue.y
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).blue.z int 1 run scoreboard players get @s hm.blue.z

# Save green marker
$execute if score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).green.active set value 1b
$execute unless score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).green.active set value 0b
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).green.x int 1 run scoreboard players get @s hm.green.x
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).green.y int 1 run scoreboard players get @s hm.green.y
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).green.z int 1 run scoreboard players get @s hm.green.z

# Save yellow marker
$execute if score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).yellow.active set value 1b
$execute unless score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).yellow.active set value 0b
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).yellow.x int 1 run scoreboard players get @s hm.yellow.x
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).yellow.y int 1 run scoreboard players get @s hm.yellow.y
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).yellow.z int 1 run scoreboard players get @s hm.yellow.z

# Save purple marker
$execute if score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).purple.active set value 1b
$execute unless score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).$(dim).purple.active set value 0b
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.x int 1 run scoreboard players get @s hm.purple.x
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.y int 1 run scoreboard players get @s hm.purple.y
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).$(dim).purple.z int 1 run scoreboard players get @s hm.purple.z


