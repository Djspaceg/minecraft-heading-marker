# Macro to remove all waypoints for this player
# Args: $(uuid)

$kill @e[type=armor_stand,tag=hm.waypoint,tag=hm.player.$(uuid)]
