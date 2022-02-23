package com.quoridor.game.object;

import java.awt.Point;

import com.quoridor.enums.ACTION_MODE;

public class GameAction implements java.io.Serializable{
    private Point point;
    private ACTION_MODE actionMode;
    private boolean verticalWall;

    public GameAction(Point point) {
        this.point = point;
        this.actionMode = ACTION_MODE.MOVE_MODE;
    }

    public GameAction(Point point, boolean verticalWall) {
        this.point = point;
        this.verticalWall = verticalWall;
        this.actionMode = ACTION_MODE.WALL_MODE;
    }

    public GameAction(String str) {
        String[] splitedStr = str.split(" ");
        int x = Integer.parseInt(splitedStr[1]);
        int y = Integer.parseInt(splitedStr[2]);
        this.point = new Point(x, y);
        if(splitedStr[0].equals(ACTION_MODE.WALL_MODE.toString())) {
            this.actionMode = ACTION_MODE.WALL_MODE;
            this.verticalWall = false;
            if(splitedStr[3].equals("vertical")) {
                this.verticalWall = true;
            }
        } else {
            this.actionMode = ACTION_MODE.MOVE_MODE;
        }
    }

    public Point getPoint() {
        return point;
    }

    public ACTION_MODE getActionMode() {
        return actionMode;
    }

    public boolean isVertical() {
        return verticalWall;
    }  

    public String toString() {
        String str = actionMode.toString() + " " + Integer.toString(point.x) + " " + Integer.toString(point.y);
        if(actionMode.isWallMode()) {
            if(verticalWall) {
                str += " vertical";
            } else {
                str += " horizontal";
            }
        }

        return str;
    }
}