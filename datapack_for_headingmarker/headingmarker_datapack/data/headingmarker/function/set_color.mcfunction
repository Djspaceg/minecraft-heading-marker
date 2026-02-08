# Validate color input (0-4)
execute unless score @s hm.input.color matches 0..4 run tellraw @s {"text":"Error: Color must be 0-4 (red/blue/green/yellow/purple)","color":"red"}
execute unless score @s hm.input.color matches 0..4 run return fail

# Get player UUID for entity tagging (CRITICAL FIX: Ensure this is called)
function headingmarker:internal/get_player_uuid

# Store coordinates and UUID in storage
execute store result storage headingmarker:temp x int 1 run scoreboard players get @s hm.input.x
execute store result storage headingmarker:temp y int 1 run scoreboard players get @s hm.input.y
execute store result storage headingmarker:temp z int 1 run scoreboard players get @s hm.input.z
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Configure params based on color input
# Red (0)
execute if score @s hm.input.color matches 0 run data modify storage headingmarker:temp color_id set value 0
execute if score @s hm.input.color matches 0 run data modify storage headingmarker:temp color_int set value 16711680
execute if score @s hm.input.color matches 0 run data modify storage headingmarker:temp name set value "red"
execute if score @s hm.input.color matches 0 run scoreboard players set @s hm.red.active 1

# Blue (1)
execute if score @s hm.input.color matches 1 run data modify storage headingmarker:temp color_id set value 1
execute if score @s hm.input.color matches 1 run data modify storage headingmarker:temp color_int set value 255
execute if score @s hm.input.color matches 1 run data modify storage headingmarker:temp name set value "blue"
execute if score @s hm.input.color matches 1 run scoreboard players set @s hm.blue.active 1

# Green (2)
execute if score @s hm.input.color matches 2 run data modify storage headingmarker:temp color_id set value 2
execute if score @s hm.input.color matches 2 run data modify storage headingmarker:temp color_int set value 65280
execute if score @s hm.input.color matches 2 run data modify storage headingmarker:temp name set value "green"
execute if score @s hm.input.color matches 2 run scoreboard players set @s hm.green.active 1

# Yellow (3)
execute if score @s hm.input.color matches 3 run data modify storage headingmarker:temp color_id set value 3
execute if score @s hm.input.color matches 3 run data modify storage headingmarker:temp color_int set value 16776960
execute if score @s hm.input.color matches 3 run data modify storage headingmarker:temp name set value "yellow"
execute if score @s hm.input.color matches 3 run scoreboard players set @s hm.yellow.active 1

# Purple (4)
execute if score @s hm.input.color matches 4 run data modify storage headingmarker:temp color_id set value 4
execute if score @s hm.input.color matches 4 run data modify storage headingmarker:temp color_int set value 10494192
execute if score @s hm.input.color matches 4 run data modify storage headingmarker:temp name set value "light_purple"
execute if score @s hm.input.color matches 4 run scoreboard players set @s hm.purple.active 1

# Remove existing waypoint of this color using the configured params
function headingmarker:internal/remove_waypoint_macro with storage headingmarker:temp

# Spawn new waypoint using the configured params
function headingmarker:internal/spawn_waypoint_macro with storage headingmarker:temp

# Configure the new waypoint
execute as @e[type=armor_stand,tag=hm.waypoint.new] run function headingmarker:internal/apply_waypoint_settings with storage headingmarker:temp

