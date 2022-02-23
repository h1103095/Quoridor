package com.quoridor.game.object;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.NETWORK_MSG_TYPE;
import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.logger.MyLogger;
import com.quoridor.networking.NetworkObject;
import com.quoridor.agents.Player;

public class NetworkGameThread extends GameThread {
    private Player localPlayer;
    private NetworkObject networkObject;

    public NetworkGameThread(GAME_MODE gameMode, Player blackPlayer, Player whitePlayer, PLAYER_COLOR startColor, NetworkObject networkObject) {
        // 호스트는 검정, 클라이언트는 하양
        this.gameState = new GameState(gameMode, blackPlayer, whitePlayer, startColor);
        this.networkObject = networkObject;
        if(gameMode == GAME_MODE.NETWORK_HOST) {
            localPlayer = blackPlayer;
        } else {
            localPlayer = whitePlayer;
        }
    }

    public void GiveUp() {
        networkObject.SendData(NETWORK_MSG_TYPE.GIVE_UP, "None");
        if(this.isAlive()) {
            this.interrupt();
        }
    }

    public void run() {
        /*
        1. 플레이어로부터 액션을 받아온다.
        2. 현재 턴 카운트 이후에 저장된 액션들을 제거한다.
        3. 받아온 액션을 저장한다.
        4. 액션을 적용한다.
        5. 턴을 바꾼다.
        */
        TurnInfo turnInfo;
        GameAction action;
        try{
            while(!gameState.isGameOver() && !Thread.currentThread().isInterrupted()) {
                Player currentPlayer = gameState.getCurrentPlayer();
                if(currentPlayer == localPlayer) {
                    action = currentPlayer.SelectAction(gameState);

                    networkObject.SendData(NETWORK_MSG_TYPE.ACTION, action.toString());
                    turnInfo = new TurnInfo(currentPlayer.getPlayerColor(), currentPlayer.getPoint(), action);
                    gameState.ProceedTurnAction(turnInfo);
                } else {
                    String msg = networkObject.ReceiveData();
                    msg = msg.split("/")[0];
                    String[] splitedMsg = msg.split(" ", 2);
                    String msgType = splitedMsg[0];
                    
                    if(msgType.equals(NETWORK_MSG_TYPE.ACTION.toString()))  {
                        action = new GameAction(splitedMsg[1]);
                        turnInfo = new TurnInfo(currentPlayer.getPlayerColor(), currentPlayer.getPoint(), action);
                        gameState.ProceedTurnAction(turnInfo);
                    } else if(msgType.equals(NETWORK_MSG_TYPE.GIVE_UP.toString())) {
                        // 항복 처리
                        gameState.GiveUp();
                        this.interrupt();
                    } else {
                        MyLogger.getInstance().warning("네트워크 상대로부터 잘못된 값이 전송되었습니다.\n");
                        this.interrupt();
                    }
                }
            }

            networkObject.CloseSocket();
        } catch (InterruptedException e) {
            networkObject.CloseSocket();
            MyLogger.getInstance().info("Thread interrupted.");
        }
    }
}