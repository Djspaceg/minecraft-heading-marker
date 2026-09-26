tellraw @s {"text":"=== Heading Marker ===","color":"gold","bold":true}
tellraw @s {"text":""}
tellraw @s [{"text":"Set Waypoint Here: ","color":"yellow"},{"text":"[Click]","color":"green","click_event":{"action":"suggest_command","command":"/function headingmarker:set_here"},"hover_event":{"action":"show_text","value":"Put the command in chat"}}]
tellraw @s [{"text":"Set Waypoint (x y z): ","color":"yellow"},{"text":"[Click]","color":"green","click_event":{"action":"suggest_command","command":"/function headingmarker:set_macro {x:1000,y:64,z:-500}"},"hover_event":{"action":"show_text","value":"Put the command in chat, then edit the coordinates"}}]
tellraw @s [{"text":"Set Waypoint (x z, y=64): ","color":"yellow"},{"text":"[Click]","color":"green","click_event":{"action":"suggest_command","command":"/function headingmarker:set_2d {x:1000,z:-500}"},"hover_event":{"action":"show_text","value":"Put the command in chat, then edit the coordinates"}}]
tellraw @s [{"text":"Remove Waypoint: ","color":"yellow"},{"text":"[Click]","color":"green","click_event":{"action":"suggest_command","command":"/function headingmarker:remove"},"hover_event":{"action":"show_text","value":"Put the command in chat"}}]
tellraw @s {"text":""}
tellraw @s {"text":"Waypoints appear in your Locator Bar.","color":"gray","italic":true}
