# Validate inputs (all three coordinates must be provided)
execute unless score @s hm.input.x = @s hm.input.x run tellraw @s {"text":"Error: X coordinate required","color":"red"}
execute unless score @s hm.input.y = @s hm.input.y run tellraw @s {"text":"Error: Y coordinate required","color":"red"}
execute unless score @s hm.input.z = @s hm.input.z run tellraw @s {"text":"Error: Z coordinate required","color":"red"}

# If player already has a waypoint, remove it first
execute if score @s hm.has.waypoint matches 1 run function headingmarker:remove

# If all inputs valid, spawn the waypoint entity
execute if score @s hm.input.x = @s hm.input.x if score @s hm.input.y = @s hm.input.y if score @s hm.input.z = @s hm.input.z run function headingmarker:internal/spawn_waypoint

# Clear input variables
scoreboard players reset @s hm.input.x
scoreboard players reset @s hm.input.y
scoreboard players reset @s hm.input.z
