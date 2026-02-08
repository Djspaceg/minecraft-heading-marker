# Set a waypoint at the player's current location
execute store result score @s hm.input.x run data get entity @s Pos[0] 1
execute store result score @s hm.input.y run data get entity @s Pos[1] 1
execute store result score @s hm.input.z run data get entity @s Pos[2] 1
function headingmarker:internal/set_core
