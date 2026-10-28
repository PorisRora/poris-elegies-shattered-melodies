package com.poris.elegies.melodies;

/** How hard a track pushes; 静 / 行 / 烈 in the BGM design doc. */
public enum Energy {
    STILL, WALK, FULL;

    static Energy parse(String name) {
        return switch(name) {
            case "still" -> STILL;
            case "walk" -> WALK;
            case "full" -> FULL;
            default -> throw new IllegalArgumentException("energy must be still, walk or full: "+name);
        };
    }

    String key(){return name().toLowerCase(java.util.Locale.ROOT);}
}
