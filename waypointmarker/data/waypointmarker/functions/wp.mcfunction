# /wp <name> <x> <y> <z>
# Usage: /function waypointmarker:wp <name> <x> <y> <z>
# Example: /function waypointmarker:wp home 100 64 -200

execute if score @s waypointmarker.waypoint_count matches ..9 run scoreboard players add @s waypointmarker.waypoint_count 1
scoreboard players set @s waypointmarker.last_waypoint 1
# Store the waypoint data in storage
data modify storage waypointmarker:players Waypoints append value {"player":"@s","name":"$1","x":$2,"y":$3,"z":$4}
tellraw @s ["",{"text":"Waypoint set: ","color":"green"},{"text":"$1 ","color":"yellow"},{"text":"($2, $3, $4)","color":"aqua"}]