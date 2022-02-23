package com.quoridor.enums;

import java.awt.Color;

public enum PLAYER_COLOR {
    BLACK(Color.BLACK),
    WHITE(Color.WHITE);

    private final Color color;

    private PLAYER_COLOR(Color color) {
        this.color = color;
    }

    public PLAYER_COLOR opponent() {
        if(this == PLAYER_COLOR.BLACK) {
            return PLAYER_COLOR.WHITE;
        } else {
            return PLAYER_COLOR.BLACK;
        }
    }

    public Color getColor() { return color; }
};
