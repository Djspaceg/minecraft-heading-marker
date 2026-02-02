# Store coordinates for summon command
execute store result storage headingmarker:temp x int 1 run scoreboard players get @s hm.input.x
execute store result storage headingmarker:temp y int 1 run scoreboard players get @s hm.input.y
execute store result storage headingmarker:temp z int 1 run scoreboard players get @s hm.input.z

# Store player UUID for entity tag
execute store result storage headingmarker:temp uuid int 1 run scoreboard players get @s hm.uuid

# Summon armor stand with green waypoint tags
function headingmarker:internal/colors/summon_green_macro with storage headingmarker:temp

# Mark as active
scoreboard players set @s hm.green.active 1

# Configure waypoint properties
execute as @e[type=armor_stand,tag=hm.waypoint.new] run function headingmarker:internal/colors/config/green

# Ensure the player activates the waypoint for their HUD (apply as player to the nearest configured waypoint)
waypoint modify @e[type=armor_stand,tag=hm.waypoint,limit=1,sort=nearest] color 65280

# Confirm
tellraw @s [{"text":"🟢 Green waypoint set at ","color":"green"},{"score":{"name":"@s","objective":"hm.input.x"}},{"text":" "},{"score":{"name":"@s","objective":"hm.input.y"}},{"text":" "},{"score":{"name":"@s","objective":"hm.input.z"}}]
