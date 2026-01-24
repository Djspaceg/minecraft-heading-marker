# Heading Marker - Remove Marker Command
# Clears lodestone tracking from compass

# Display instructions for removing markers
tellraw @s ["",{"text":"[Heading Marker] ","color":"gold","bold":true},{"text":"To remove a heading marker:","color":"yellow"}]
tellraw @s ["",{"text":"Option 1: ","color":"gray"},{"text":"Break the Lodestone","color":"aqua"},{"text":" - The compass will spin randomly again","color":"yellow"}]
tellraw @s ["",{"text":"Option 2: ","color":"gray"},{"text":"Use an anvil","color":"aqua"},{"text":" to remove the lodestone tracking","color":"yellow"}]
tellraw @s ["",{"text":"Option 3: ","color":"gray"},{"text":"Simply drop/destroy","color":"aqua"},{"text":" the lodestone compass","color":"yellow"}]

# Play a sound effect
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.8
