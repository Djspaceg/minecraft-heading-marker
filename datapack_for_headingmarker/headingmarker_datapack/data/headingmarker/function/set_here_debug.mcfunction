# Minimal debug set_here (safe, guaranteed to compile)
# Get UUID first!
function headingmarker:internal/get_player_uuid

# Store player's current position
execute store result score @s hm.input.x run data get entity @s Pos[0] 1
execute store result score @s hm.input.y run data get entity @s Pos[1] 1
execute store result score @s hm.input.z run data get entity @s Pos[2] 1

# Use the existing summon macro so the stand gets `hm.player.$(uuid)` tag (required by client locator)
# Store the data the summon macro expects
execute store result storage headingmarker:temp x int 1 run scoreboard players get @s hm.input.x
execute store result storage headingmarker:temp y int 1 run scoreboard players get @s hm.input.y
execute store result storage headingmarker:temp z int 1 run scoreboard players get @s hm.input.z
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Set debug color params (Red)
data modify storage headingmarker:temp color_id set value 0
data modify storage headingmarker:temp color_int set value 16711680
data modify storage headingmarker:temp name set value "red"

# Summon a visible debug stand via the macro
function headingmarker:internal/spawn_waypoint_debug_macro with storage headingmarker:temp

# Configure the new waypoint EXPLICITLY here (removed from macro file)
execute as @e[type=armor_stand,tag=hm.waypoint.new] run function headingmarker:internal/apply_waypoint_settings with storage headingmarker:temp


# Dump nearest hm.waypoint.debug entity NBT to log
execute as @s run tellraw @s {"text":"--- DEBUG START ---", "color":"gold"}
execute as @e[type=armor_stand,tag=hm.waypoint.debug,limit=1,sort=nearest] run data get entity @s
execute as @s run tellraw @s {"text":"--- DEBUG END ---", "color":"gold"}

# Mark player as having a waypoint
scoreboard players set @s hm.has.waypoint 1

# Confirm debug marker created
tellraw @s [{"text":"Debug waypoint created (visible + configured)","color":"green"}]
