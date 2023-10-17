package com.quoridor.game.object;

import java.util.Vector;
import javax.swing.JOptionPane;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.logger.MyLogger;
import com.quoridor.agents.Player;

public class ReplayThread extends GameThread {
    private boolean pause = false;
    private boolean giveUp = false;
    private boolean giveUpPaneAppeared = false;

    public ReplayThread(GAME_MODE gameMode, Player blackPlayer, Player whitePlayer, PLAYER_COLOR startColor, Vector<TurnInfo> history, boolean giveUp) {
        this.giveUp = giveUp;
        gameState = new GameState(gameMode, blackPlayer, whitePlayer, startColor);
        gameState.setHistory(history);
    }

    public void run() {
        try {
            Thread.sleep(1000);
            while(!Thread.currentThread().isInterrupted()) {
                if(!pause) {
                    if(gameState.checkCanMoveToNext()) {
                        gameState.moveToNextTurn();
                    } else if(giveUp && !giveUpPaneAppeared) {
                        String msg = gameState.getCurrentPlayer().getPlayerName() + "(" + gameState.getCurrentPlayer().getPlayerColor().toString() + ")이(가) 항복하였습니다.";
                        JOptionPane.showMessageDialog(null, msg, "항복", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
                        giveUpPaneAppeared = true;
                    }
                }
                Thread.sleep(1000);   
            }
        } catch(InterruptedException e) {
            MyLogger.getInstance().info("Thread interrupted.");
        }
    }

    public void pause() {
        pause = true;
    }

    public void play() {
        pause = false;
    }

    public boolean isPause() {
        return pause;
    }
}
