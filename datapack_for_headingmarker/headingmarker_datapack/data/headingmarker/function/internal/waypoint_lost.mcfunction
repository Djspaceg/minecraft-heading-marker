# Clear the flag
scoreboard players set @s hm.has.waypoint 0

# Notify player
tellraw @s {"text":"Your waypoint was destroyed!","color":"red"}
