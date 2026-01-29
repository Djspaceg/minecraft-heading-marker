# Diagnostic: Inspect waypoint entities for the executing player
# Usage: /function headingmarker:internal/debug/inspect_waypoint

# Get player uuid into score
function headingmarker:internal/get_player_uuid

# Basic existence checks
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid)] run tellraw @s {"text":"Found at least one armor_stand tagged hm.player.$(uuid)","color":"green"}
execute unless entity @e[type=armor_stand,tag=hm.player.$(uuid)] run tellraw @s {"text":"No armor_stand with hm.player.$(uuid) found","color":"red"}

# Check for waypoint tag and "new" tag presence
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),tag=hm.waypoint] run tellraw @s {"text":"Armor stand has tag hm.waypoint","color":"green"}
execute unless entity @e[type=armor_stand,tag=hm.player.$(uuid),tag=hm.waypoint] run tellraw @s {"text":"Armor stand missing tag hm.waypoint","color":"red"}
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),tag=hm.waypoint.new] run tellraw @s {"text":"Armor stand has tag hm.waypoint.new (pending config)","color":"green"}

# Check for waypoint attribute presence (transmission_range)
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"minecraft:waypoint.transmission_range"}]}] run tellraw @s {"text":"Found attribute minecraft:waypoint.transmission_range on waypoint","color":"green"}
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"waypoint_transmission_range"}]}] run tellraw @s {"text":"Found attribute waypoint_transmission_range on waypoint","color":"green"}
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"minecraft:waypoint_transmission_range"}]}] run tellraw @s {"text":"Found attribute minecraft:waypoint_transmission_range on waypoint","color":"green"}
execute unless entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"minecraft:waypoint.transmission_range"}]}] unless entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"waypoint_transmission_range"}]}] unless entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Attributes:[{Name:"minecraft:waypoint_transmission_range"}]}] run tellraw @s {"text":"No transmission_range attribute present on waypoint","color":"red"}

# Check Marker / Invisible flags
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Marker:1b}] run tellraw @s {"text":"Entity has Marker:1b","color":"yellow"}
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Invisible:1b}] run tellraw @s {"text":"Entity is Invisible:1b","color":"yellow"}
execute if entity @e[type=armor_stand,tag=hm.player.$(uuid),nbt={Invisible:0b}] run tellraw @s {"text":"Entity is Visible (Invisible:0b)","color":"yellow"}

# Output the full NBT of the nearest matching armor stand for inspection
execute as @s at @s run data get entity @e[type=armor_stand,tag=hm.player.$(uuid),limit=1,sort=nearest]

# Also show server-side waypoint count the datapack is tracking (hm.waypoint.count exists per player checks)
execute if score @s hm.waypoint.count matches * run tellraw @s [{"text":"hm.waypoint.count = "},{"score":{"name":"@s","objective":"hm.waypoint.count"}}]
