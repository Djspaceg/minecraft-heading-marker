# Iterate all registered waypoint entities and re-apply their settings based on color tag
# Red (0)
execute as @e[type=armor_stand,tag=hm.waypoint,tag=hm.color.0] run function headingmarker:internal/apply_waypoint_settings {color_int:16711680}
# Blue (1)
execute as @e[type=armor_stand,tag=hm.waypoint,tag=hm.color.1] run function headingmarker:internal/apply_waypoint_settings {color_int:255}
# Green (2)
execute as @e[type=armor_stand,tag=hm.waypoint,tag=hm.color.2] run function headingmarker:internal/apply_waypoint_settings {color_int:65280}
# Yellow (3)
execute as @e[type=armor_stand,tag=hm.waypoint,tag=hm.color.3] run function headingmarker:internal/apply_waypoint_settings {color_int:16776960}
# Purple (4)
execute as @e[type=armor_stand,tag=hm.waypoint,tag=hm.color.4] run function headingmarker:internal/apply_waypoint_settings {color_int:10494192}

