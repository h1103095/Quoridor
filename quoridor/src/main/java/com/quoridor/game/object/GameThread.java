package com.quoridor.game.object;

public class GameThread extends Thread {
    protected GameState gameState;

    public void giveUp() {
        if(this.isAlive()) {
            this.interrupt();
        }
    }

    public void run() {}

    public GameState getGameState() {
        return gameState;
    }
}
