package com.quoridor.agents;

import java.awt.*;
import java.util.concurrent.BlockingQueue;

import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.game.object.GameAction;
import com.quoridor.game.object.GameState;

/*
 * 플레이어의 정보
 */

public class Player {
	private Point point;
	private PLAYER_COLOR playerColor;
	private String playerName;	// 플레이어명
	private BlockingQueue<GameAction> queue;

	protected int numWalls; // 벽의 개수
	
	public Player(PLAYER_COLOR playerNumber, int wallNum, String playerName, BlockingQueue<GameAction> queue) {
		this.playerColor = playerNumber;
		this.numWalls = wallNum;
		this.playerName = playerName;
		this.queue = queue;
	}
	
	public PLAYER_COLOR getPlayerColor() { return playerColor; }
	public Point getPoint() { return point; }
	public int getNumRemainWalls() { return numWalls; }
	public boolean checkWallRemains() { return numWalls > 0; }
	public void decreaseNumWalls() { numWalls--; }
	public void increaseNumWalls() { numWalls++; }
	public void move(Point p) { this.point = p; }
	public void setPlayerName(String playerName) { this.playerName = playerName; }
	public String getPlayerName() { return playerName; }

	public GameAction selectAction(GameState gameState) throws InterruptedException{
		GameAction action;
		queue.clear();
		do {
			action = queue.take();
		} while (!gameState.checkAvailableAction(action));
		return action;
	}
}