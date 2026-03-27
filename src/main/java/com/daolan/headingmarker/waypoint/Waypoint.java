package com.daolan.headingmarker.waypoint;

import java.util.Optional;

public interface Waypoint {
    Config getConfig();

    class Config {
        public Optional<Integer> color = Optional.empty();
    }
}
