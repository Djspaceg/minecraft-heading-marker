# Internal: Save Player Markers Macro
# Macro that receives ID and saves markers to ID-keyed storage

# Save red marker
$execute if score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(id).red.active set value 1b
$execute unless score @s hm.red.active matches 1 run data modify storage headingmarker:players.$(id).red.active set value 0b
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).red.x int 1 run scoreboard players get @s hm.red.x
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).red.y int 1 run scoreboard players get @s hm.red.y
$execute if score @s hm.red.active matches 1 store result storage headingmarker:players.$(id).red.z int 1 run scoreboard players get @s hm.red.z

# Save blue marker
$execute if score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(id).blue.active set value 1b
$execute unless score @s hm.blue.active matches 1 run data modify storage headingmarker:players.$(id).blue.active set value 0b
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).blue.x int 1 run scoreboard players get @s hm.blue.x
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).blue.y int 1 run scoreboard players get @s hm.blue.y
$execute if score @s hm.blue.active matches 1 store result storage headingmarker:players.$(id).blue.z int 1 run scoreboard players get @s hm.blue.z

# Save green marker
$execute if score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(id).green.active set value 1b
$execute unless score @s hm.green.active matches 1 run data modify storage headingmarker:players.$(id).green.active set value 0b
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).green.x int 1 run scoreboard players get @s hm.green.x
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).green.y int 1 run scoreboard players get @s hm.green.y
$execute if score @s hm.green.active matches 1 store result storage headingmarker:players.$(id).green.z int 1 run scoreboard players get @s hm.green.z

# Save yellow marker
$execute if score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(id).yellow.active set value 1b
$execute unless score @s hm.yellow.active matches 1 run data modify storage headingmarker:players.$(id).yellow.active set value 0b
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(id).yellow.x int 1 run scoreboard players get @s hm.yellow.x
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(id).yellow.y int 1 run scoreboard players get @s hm.yellow.y
$execute if score @s hm.yellow.active matches 1 store result storage headingmarker:players.$(id).yellow.z int 1 run scoreboard players get @s hm.yellow.z

# Save purple marker
$execute if score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).purple.active set value 1b
$execute unless score @s hm.purple.active matches 1 run data modify storage headingmarker:players.$(uuid).purple.active set value 0b
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).purple.x int 1 run scoreboard players get @s hm.purple.x
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).purple.y int 1 run scoreboard players get @s hm.purple.y
$execute if score @s hm.purple.active matches 1 store result storage headingmarker:players.$(uuid).purple.z int 1 run scoreboard players get @s hm.purple.z


