# Generic macro to remove a waypoint
# Args: $(uuid), $(color_id)

$kill @e[type=armor_stand,tag=hm.waypoint,tag=hm.player.$(uuid),tag=hm.color.$(color_id)]
