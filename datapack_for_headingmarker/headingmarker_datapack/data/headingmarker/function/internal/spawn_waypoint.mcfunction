# Store coordinates in temporary storage for summon command
execute store result storage headingmarker:temp x int 1 run scoreboard players get @s hm.input.x
execute store result storage headingmarker:temp y int 1 run scoreboard players get @s hm.input.y
execute store result storage headingmarker:temp z int 1 run scoreboard players get @s hm.input.z

# Summon invisible armor stand at coordinates with special tags
function headingmarker:internal/summon_macro with storage headingmarker:temp

# Mark player as having a waypoint
scoreboard players set @s hm.has.waypoint 1

# Configure the waypoint properties
function headingmarker:internal/configure_waypoint

# Confirm to player
tellraw @s [{"text":"Waypoint set at ","color":"green"},{"score":{"name":"@s","objective":"hm.input.x"},"color":"yellow"},{"text":" "},{"score":{"name":"@s","objective":"hm.input.y"},"color":"yellow"},{"text":" "},{"score":{"name":"@s","objective":"hm.input.z"},"color":"yellow"}]
tellraw @s {"text":"Check your Locator Bar!","color":"gray","italic":true}
