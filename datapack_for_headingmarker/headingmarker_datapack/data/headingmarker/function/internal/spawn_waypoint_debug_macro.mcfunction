# Generic macro to spawn a VISIBLE debug waypoint
# Args: $(x), $(y), $(z), $(uuid), $(color_id), $(color_int), $(name)

$summon minecraft:armor_stand $(x) $(y) $(z) {Tags:["hm.waypoint","hm.waypoint.new","hm.waypoint.debug","hm.player.$(uuid)","hm.color.$(color_id)"],Invisible:0b,Invulnerable:1b,NoGravity:1b,Silent:1b,Marker:1b,CustomName:'{"text":"DEBUG $(name) Waypoint","color":"$(name)","italic":true}',Attributes:[{id:"minecraft:waypoint.transmission_range",base:6000000.0d},{id:"waypoint_transmission_range",base:6000000.0d},{id:"minecraft:waypoint_transmission_range",base:6000000.0d},{id:"minecraft:generic.max_health",base:20.0d}]

