# Summon with player-specific and color-specific tags
$summon minecraft:armor_stand $(x) $(y) $(z) {Tags:["hm.waypoint","hm.waypoint.red","hm.player.$(uuid)","hm.waypoint.new"],Invisible:1b,Invulnerable:1b,NoGravity:1b,Silent:1b,Marker:1b,CustomName:'{"text":"Red Waypoint","color":"red"}'}
