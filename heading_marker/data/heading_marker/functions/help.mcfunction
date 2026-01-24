# Heading Marker - Help Command
# Shows usage information and examples for all commands

tellraw @s ["",{"text":"========================================","color":"gold"}]
tellraw @s ["",{"text":"Heading Marker - Command Help","color":"gold","bold":true}]
tellraw @s ["",{"text":"========================================","color":"gold"}]
tellraw @s ""

# Set marker commands
tellraw @s ["",{"text":"SET MARKER:","color":"aqua","bold":true}]
tellraw @s ["",{"text":"• 2D Mode (Y=64): ","color":"gray"},{"text":"/function heading_marker:set_2d {x:1000, z:-500}","color":"yellow","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set_2d {x:1000, z:-500}"},"hoverEvent":{"action":"show_text","contents":"Click to copy"}}]
tellraw @s ["",{"text":"• 3D Mode: ","color":"gray"},{"text":"/function heading_marker:set {x:1000, y:64, z:-500}","color":"yellow","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set {x:1000, y:64, z:-500}"},"hoverEvent":{"action":"show_text","contents":"Click to copy"}}]
tellraw @s ["",{"text":"• 2D With Color: ","color":"gray"},{"text":"/function heading_marker:set_2d_color {x:1000, z:-500, color:2}","color":"yellow","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set_2d_color {x:1000, z:-500, color:2}"},"hoverEvent":{"action":"show_text","contents":"Click to copy"}}]
tellraw @s ["",{"text":"• 3D With Color: ","color":"gray"},{"text":"/function heading_marker:set_3d_color {x:1000, y:64, z:-500, color:2}","color":"yellow","clickEvent":{"action":"suggest_command","value":"/function heading_marker:set_3d_color {x:1000, y:64, z:-500, color:2}"},"hoverEvent":{"action":"show_text","contents":"Click to copy"}}]
tellraw @s ""

# Remove marker command
tellraw @s ["",{"text":"REMOVE MARKER:","color":"aqua","bold":true}]
tellraw @s ["",{"text":"• Remove by color: ","color":"gray"},{"text":"/function heading_marker:remove {color:0}","color":"yellow","clickEvent":{"action":"suggest_command","value":"/function heading_marker:remove {color:0}"},"hoverEvent":{"action":"show_text","contents":"Click to copy"}}]
tellraw @s ""

# Color reference
tellraw @s ["",{"text":"COLORS:","color":"aqua","bold":true}]
tellraw @s ["",{"text":"• 0 = ","color":"gray"},{"text":"🔴 Red","color":"red"}]
tellraw @s ["",{"text":"• 1 = ","color":"gray"},{"text":"🔵 Blue","color":"blue"}]
tellraw @s ["",{"text":"• 2 = ","color":"gray"},{"text":"🟢 Green","color":"green"}]
tellraw @s ["",{"text":"• 3 = ","color":"gray"},{"text":"🟡 Yellow","color":"yellow"}]
tellraw @s ["",{"text":"• 4 = ","color":"gray"},{"text":"🟣 Purple","color":"light_purple"}]
tellraw @s ""

# Quick examples
tellraw @s ["",{"text":"QUICK EXAMPLES:","color":"aqua","bold":true}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"Example 1: ","color":"white"},{"text":"[Click] ","color":"green","clickEvent":{"action":"suggest_command","value":"/function heading_marker:examples/home"},"hoverEvent":{"action":"show_text","contents":"Set red marker at spawn"}},{"text":"Mark home base","color":"gray"}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"Example 2: ","color":"white"},{"text":"[Click] ","color":"green","clickEvent":{"action":"suggest_command","value":"/function heading_marker:examples/mine"},"hoverEvent":{"action":"show_text","contents":"Set blue marker underground"}},{"text":"Mark mine location","color":"gray"}]
tellraw @s ["",{"text":"• ","color":"gray"},{"text":"Example 3: ","color":"white"},{"text":"[Click] ","color":"green","clickEvent":{"action":"suggest_command","value":"/function heading_marker:examples/portal"},"hoverEvent":{"action":"show_text","contents":"Set purple marker for portal"}},{"text":"Mark nether portal","color":"gray"}]
tellraw @s ""

tellraw @s ["",{"text":"========================================","color":"gold"}]
tellraw @s ["",{"text":"Tip: ","color":"gold"},{"text":"Click commands to copy them, then edit coordinates!","color":"yellow"}]
tellraw @s ["",{"text":"========================================","color":"gold"}]

# Play a sound effect
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1.5
