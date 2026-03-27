package com.daolan.headingmarker.waypoint

import java.util.Optional

interface Waypoint {
    val config: Config

    class Config {
        var color: Optional<Int> = Optional.empty()
    }
}
