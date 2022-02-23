package com.quoridor.game.object;

import java.awt.Point;

import com.quoridor.enums.PLAYER_COLOR;


public class TurnInfo implements java.io.Serializable{
    private PLAYER_COLOR playerColor;
    private Point startPoint;
    private GameAction action;

    public TurnInfo(PLAYER_COLOR playerColor, Point startPoint, GameAction action) {
        this.playerColor = playerColor;
        this.startPoint = startPoint;
        this.action = action;
    }

    public PLAYER_COLOR getPlayerColor() { return playerColor; }
    public Point getStartPoint() { return startPoint; }
    public GameAction getAction() { return action; }
}
