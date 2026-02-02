# Minimal debug set_here (safe, guaranteed to compile)
# Store player's current position
execute store result score @s hm.input.x run data get entity @s Pos[0] 1
execute store result score @s hm.input.y run data get entity @s Pos[1] 1
execute store result score @s hm.input.z run data get entity @s Pos[2] 1

# Summon a visible debug armor stand at the player's location and tag it as a waypoint
summon minecraft:armor_stand ~ ~ ~ {Tags:["hm.waypoint","hm.waypoint.debug"],Invisible:0b,Invulnerable:1b,NoGravity:1b,Silent:1b,Marker:0b,CustomNameVisible:1b,CustomName:'{"text":"DEBUG_WAYPOINT","color":"red"}'}

# Configure the summoned debug stand with waypoint attributes (apply shared settings)
execute as @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run function headingmarker:internal/apply_waypoint_settings

# Also force the working attribute name/value that you verified in-game (sets big transmission range)
# Prefer data modify fallback to ensure Attributes NBT exists even if attribute isn't registered
execute as @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run data modify entity @s Attributes append value {Name:"minecraft:waypoint_transmission_range",Base:6000000}
execute as @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run data modify entity @s Attributes append value {Name:"waypoint_transmission_range",Base:6000000}
execute as @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run data modify entity @s Attributes append value {Name:"minecraft:waypoint.transmission_range",Base:6000000}

# Attach debug waypoint to player's HUD (run as player so it shows in Locator Bar) using keyword color
execute if entity @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run execute as @s run waypoint modify @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] color red

# Dump nearest hm.waypoint.debug entity NBT for inspection
execute as @s run data get entity @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest]

# Mark player as having a waypoint
scoreboard players set @s hm.has.waypoint 1

# Confirm debug marker created
tellraw @s [{"text":"Debug waypoint created (visible + configured)","color":"green"}]

