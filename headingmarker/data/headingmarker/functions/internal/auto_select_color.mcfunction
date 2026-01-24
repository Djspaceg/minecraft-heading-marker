# Internal: Auto-select next color
# Finds the next unused color, or cycles to next in sequence

# Initialize next color if not set
execute unless score @s hm.nextcolor matches 0..4 run scoreboard players set @s hm.nextcolor 0

# Start with current next color
scoreboard players operation @s hm.input.color = @s hm.nextcolor

# Check up to 5 times to find an unused color
execute if score @s hm.input.color matches 0 if score @s hm.red.active matches 1 run scoreboard players add @s hm.input.color 1
execute if score @s hm.input.color matches 5.. run scoreboard players set @s hm.input.color 0

execute if score @s hm.input.color matches 1 if score @s hm.blue.active matches 1 run scoreboard players add @s hm.input.color 1
execute if score @s hm.input.color matches 5.. run scoreboard players set @s hm.input.color 0

execute if score @s hm.input.color matches 2 if score @s hm.green.active matches 1 run scoreboard players add @s hm.input.color 1
execute if score @s hm.input.color matches 5.. run scoreboard players set @s hm.input.color 0

execute if score @s hm.input.color matches 3 if score @s hm.yellow.active matches 1 run scoreboard players add @s hm.input.color 1
execute if score @s hm.input.color matches 5.. run scoreboard players set @s hm.input.color 0

execute if score @s hm.input.color matches 4 if score @s hm.purple.active matches 1 run scoreboard players add @s hm.input.color 1
execute if score @s hm.input.color matches 5.. run scoreboard players set @s hm.input.color 0

# If all colors are used, just use the next in sequence (will overwrite)
# Update next color for next time (increment and wrap)
scoreboard players operation @s hm.nextcolor = @s hm.input.color
scoreboard players add @s hm.nextcolor 1
execute if score @s hm.nextcolor matches 5.. run scoreboard players set @s hm.nextcolor 0


