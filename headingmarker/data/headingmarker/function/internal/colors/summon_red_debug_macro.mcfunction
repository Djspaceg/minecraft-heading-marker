# Summon a visible debug armor stand at exact coordinates with player tag
$summon minecraft:armor_stand $(x) $(y) $(z) {Tags:["hm.waypoint","hm.waypoint.red","hm.player.$(uuid)","hm.waypoint.new","hm.waypoint.debug"],Invisible:0b,Invulnerable:1b,NoGravity:1b,Silent:1b,Marker:0b,CustomNameVisible:1b,CustomName:'{"text":"DEBUG_WAYPOINT","color":"red"}'}
