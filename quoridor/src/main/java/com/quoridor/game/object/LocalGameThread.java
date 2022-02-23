package com.quoridor.game.object;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.logger.MyLogger;
import com.quoridor.agents.Player;

public class LocalGameThread extends GameThread {
    public LocalGameThread(GAME_MODE gameMode, Player blackPlayer, Player whitePlayer, PLAYER_COLOR startColor) {
        this.gameState = new GameState(gameMode, blackPlayer, whitePlayer, startColor);
    }

    public void run() {
        /*
        1. 플레이어로부터 액션을 받아온다.
        2. 현재 턴 카운트 이후에 저장된 액션들을 제거한다.
        3. 받아온 액션을 저장한다.
        4. 액션을 적용한다.
        5. 턴을 바꾼다.
        */
        try{
            while(!gameState.isGameOver() && !Thread.currentThread().isInterrupted()) {
                Player currentPlayer = gameState.getCurrentPlayer();
                GameAction action = currentPlayer.SelectAction(gameState);
                TurnInfo turnInfo = new TurnInfo(currentPlayer.getPlayerColor(), currentPlayer.getPoint(), action);
                gameState.ProceedTurnAction(turnInfo);
            }
        } catch (InterruptedException e) {
            MyLogger.getInstance().info("Local Game Thread interrupted.");
        }
    }
}