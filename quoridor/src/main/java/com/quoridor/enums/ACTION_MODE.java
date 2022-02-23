package com.quoridor.enums;

public enum ACTION_MODE {
    MOVE_MODE(false),
    WALL_MODE(true);

    private final boolean wallMode;

    private ACTION_MODE(boolean wallMode) {
        this.wallMode = wallMode;
    }

    public ACTION_MODE changeMode() {
        if(this == ACTION_MODE.MOVE_MODE) {
            return ACTION_MODE.WALL_MODE;
        } else {
            return ACTION_MODE.MOVE_MODE;
        }
    }

    public boolean isWallMode() { return wallMode; }
}
