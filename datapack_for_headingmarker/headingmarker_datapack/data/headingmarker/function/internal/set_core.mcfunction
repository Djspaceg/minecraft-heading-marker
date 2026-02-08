# Core logic for setting a waypoint (Validation + Delegation)
# This function expects hm.input.x, hm.input.y, hm.input.z to be set in scoreboards.

# Validate inputs (all three coordinates must be provided)
execute unless score @s hm.input.x = @s hm.input.x run tellraw @s {"text":"Error: X coordinate required","color":"red"}
execute unless score @s hm.input.y = @s hm.input.y run tellraw @s {"text":"Error: Y coordinate required","color":"red"}
execute unless score @s hm.input.z = @s hm.input.z run tellraw @s {"text":"Error: Z coordinate required","color":"red"}

# Auto-cycle color (0->1->2->3->4->0)
scoreboard players add @s hm.nextcolor 1
execute if score @s hm.nextcolor matches 5.. run scoreboard players set @s hm.nextcolor 0

# Set input color from nextcolor
scoreboard players operation @s hm.input.color = @s hm.nextcolor

# Call set_color
execute if score @s hm.input.x = @s hm.input.x if score @s hm.input.y = @s hm.input.y if score @s hm.input.z = @s hm.input.z run function headingmarker:set_color

# Clear input variables
scoreboard players reset @s hm.input.x
scoreboard players reset @s hm.input.y
scoreboard players reset @s hm.input.z
