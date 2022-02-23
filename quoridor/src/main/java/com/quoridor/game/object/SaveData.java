package com.quoridor.game.object;

import java.util.Vector;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.GAME_OPTION;
import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.game.manager.OptionManager;

public class SaveData implements java.io.Serializable{
    private boolean emptyData = false;
    private GAME_MODE gameMode;
    private String blackPlayerName;
    private String whitePlayerName;
    private PLAYER_COLOR startColor;
    private int numWalls;
    private Vector<TurnInfo> history;
    private boolean giveUp;
    private boolean completed;

    public SaveData() {
        emptyData = true;
    }

    public SaveData(GameState gameState) {
        this.gameMode = gameState.getGameMode();
        this.blackPlayerName = gameState.getBlackPlayer().getPlayerName();
        this.whitePlayerName = gameState.getWhitePlayer().getPlayerName();
        this.startColor = gameState.getStartColor();
        this.numWalls = Integer.parseInt(OptionManager.getInstance().GetConfig(GAME_OPTION.NUM_WALLS));
        this.history = gameState.getHistory();
        this.giveUp = gameState.isGiveUp();
        this.completed = gameState.isGameOver();
    }

    public GAME_MODE getGameMode() { return gameMode; }
    public String getBlackPlayerName() { return blackPlayerName; }
    public String getWhitePlayerName() { return whitePlayerName; }
    public PLAYER_COLOR getStartColor() { return startColor; }
    public int getNumWalls() { return numWalls; }
    public Vector<TurnInfo> getHistory() { return history; }
    public boolean isGiveUp() { return giveUp; }
    public boolean isCompleted() { return completed; }

    public boolean isEmpty() {
        return emptyData;
    }

    public static SaveData getEmptyData() {
        return new SaveData();
    }
}
