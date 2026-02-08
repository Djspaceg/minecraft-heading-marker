# Public API: Set waypoint from macro arguments
# Usage: /function headingmarker:set {x:100, y:64, z:-200}

$scoreboard players set @s hm.input.x $(x)
$scoreboard players set @s hm.input.y $(y)
$scoreboard players set @s hm.input.z $(z)

function headingmarker:internal/set_core
