tellraw @s {"text":"=== Heading Marker ===","color":"gold","bold":true}
tellraw @s {"text":""}
tellraw @s [{"text":"Set Waypoint (3D): ","color":"yellow"},{"text":"[Click]","color":"green","clickEvent":{"action":"suggest_command","value":"/function headingmarker:set_macro {x:1000,y:64,z:-500}"},"hoverEvent":{"action":"show_text","value":"Click to copy command"}}]
tellraw @s [{"text":"Set Waypoint (2D): ","color":"yellow"},{"text":"[Click]","color":"green","clickEvent":{"action":"suggest_command","value":"/function headingmarker:set_2d {x:1000,z:-500}"},"hoverEvent":{"action":"show_text","value":"Click to copy command"}}]
tellraw @s [{"text":"Remove Waypoint: ","color":"yellow"},{"text":"[Click]","color":"green","clickEvent":{"action":"suggest_command","value":"/function headingmarker:remove"},"hoverEvent":{"action":"show_text","value":"Click to remove waypoint"}}]
tellraw @s {"text":""}
tellraw @s {"text":"Waypoints appear in your Locator Bar with distance!","color":"gray","italic":true}
