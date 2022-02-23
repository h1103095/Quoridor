package com.quoridor.game.manager;

import java.nio.file.*;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.*;
import javax.swing.JOptionPane;

import com.quoridor.agents.AI;
import com.quoridor.agents.Player;
import com.quoridor.networking.NetworkObject;
import com.quoridor.scene.GameFrame;
import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.GAME_OPTION;
import com.quoridor.enums.NETWORK_MSG_TYPE;
import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.game.object.LocalGameThread;
import com.quoridor.game.object.NetworkGameThread;
import com.quoridor.game.object.ReplayThread;
import com.quoridor.game.object.GameAction;
import com.quoridor.game.object.GameState;
import com.quoridor.game.object.GameThread;
import com.quoridor.game.object.SaveData;
import com.quoridor.game.object.TurnInfo;
import com.quoridor.logger.MyLogger;
import com.quoridor.utils.Utils;

/*
 * 게임의 정보를 관리하는 클래스
 */
public class GameManager {
	private OptionManager optionManager = OptionManager.getInstance();
	private String playerName = optionManager.GetConfig(GAME_OPTION.PLAYER_NAME);
	private String completedSaveDirectory = optionManager.GetConfig(GAME_OPTION.COMPLETED_GAME_SAVE_DIRECTORY);
	private String incompletedSaveDirectory = optionManager.GetConfig(GAME_OPTION.INCOMPLETED_GAME_SAVE_DIRECTORY);
	private int numWalls = Integer.parseInt(optionManager.GetConfig(GAME_OPTION.NUM_WALLS));

	private GameFrame gameFrame;
	private GameThread gameThread;
	private GameState gameState;
	private GAME_MODE gameMode;

	private BlockingQueue<GameAction> queue;

	
	// 싱글 또는 멀티일 때
	public GameManager(GAME_MODE gameMode) {
		assert (gameMode == GAME_MODE.SINGLE || gameMode == GAME_MODE.MULTY);
		this.gameMode = gameMode;
		queue = new ArrayBlockingQueue<GameAction>(10);
		Player blackPlayer;
		Player whitePlayer;

		if(gameMode == GAME_MODE.SINGLE) {
			blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, playerName, queue);
			whitePlayer = new AI(PLAYER_COLOR.WHITE, numWalls, "AI"); // AI 생성
		} else {
			blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, "BLACK", queue); // 2인 모드일 때는 설정한 이름 대신 BLACK / WHITE로 이름 설정 ;
			whitePlayer = new Player(PLAYER_COLOR.WHITE, numWalls, "WHITE", queue);
		}

		PLAYER_COLOR startColor = getRandomStartPlayer();
		gameThread = new LocalGameThread(this.gameMode, blackPlayer, whitePlayer, startColor);
		gameState = gameThread.getGameState();

		StartGame();
	}

	public GameManager(GAME_MODE gameMode, NetworkObject networkObject) {
		// 호스트는 검정, 클라이언트는 하양
		assert (gameMode == GAME_MODE.NETWORK_GUEST || gameMode == GAME_MODE.NETWORK_HOST);
		this.gameMode = gameMode;
		queue = new ArrayBlockingQueue<GameAction>(10);
		PLAYER_COLOR startColor = PLAYER_COLOR.BLACK;
		Player blackPlayer;
		Player whitePlayer;
		String opponentPlayerName = "Opponent";

		networkObject.SendData(NETWORK_MSG_TYPE.PLAYER_NAME, playerName);

		if(gameMode == GAME_MODE.NETWORK_HOST) {
			// 벽의 개수, 시작 플레이어 정보를 보냄
			startColor = getRandomStartPlayer();
			networkObject.SendData(NETWORK_MSG_TYPE.NUM_WALLS, Integer.toString(numWalls));
			networkObject.SendData(NETWORK_MSG_TYPE.FIRST_PLAYER, startColor.toString());

			// 상대 플레이어 이름을 받음
			String msg = networkObject.ReceiveData();
			String[] msgArray = msg.split("/");
			String[] splitedMsg = msgArray[0].split(" ", 2);

			String msgType = splitedMsg[0];

			if(msgType.equals(NETWORK_MSG_TYPE.PLAYER_NAME.toString())) {
				opponentPlayerName = splitedMsg[1];
			}

			blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, playerName, queue);
			whitePlayer = new Player(PLAYER_COLOR.WHITE, numWalls, opponentPlayerName, null);
		} else {
			// 서버로부터 데이터를 받음
			int receiveCount = 0;
			while(receiveCount < 3) {
				String msg = networkObject.ReceiveData();
				String[] msgArray = msg.split("/");
				for(int i=0; i < msgArray.length; i++) {
					String[] splitedMsg = msgArray[i].split(" ", 2);

					String msgType = splitedMsg[0];
					if(msgType.equals(NETWORK_MSG_TYPE.PLAYER_NAME.toString())) {
						opponentPlayerName = splitedMsg[1];
					} else if(msgType.equals(NETWORK_MSG_TYPE.NUM_WALLS.toString())) {
						numWalls = Integer.parseInt(splitedMsg[1]);
					} else if(msgType.equals(NETWORK_MSG_TYPE.FIRST_PLAYER.toString())) {
						startColor = PLAYER_COLOR.valueOf(splitedMsg[1]);
					} else {
						MyLogger.getInstance().warning("Unknown message: " + msgArray[i]);
					}
					receiveCount++;
				}
			}
			MyLogger.getInstance().info("서버로부터 받은 데이터: ");
			MyLogger.getInstance().info("서버 플레이어 이름: " + opponentPlayerName);
			MyLogger.getInstance().info("벽 개수: " + Integer.toString(numWalls));
			MyLogger.getInstance().info("시작 플레이어: " + startColor.toString());

			blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, opponentPlayerName, null);
			whitePlayer = new Player(PLAYER_COLOR.WHITE, numWalls, playerName, queue);
		}

		gameThread = new NetworkGameThread(this.gameMode, blackPlayer, whitePlayer, startColor, networkObject);
		gameState = gameThread.getGameState();

		StartGame();
	}
	
	// 게임 로드 또는 리플레이 시
	public GameManager(GAME_MODE gameMode, File file) {
		assert(gameMode == GAME_MODE.LOAD_GAME || gameMode == GAME_MODE.REPLAY);
		this.gameMode = gameMode;
		
		SaveData saveData = Load(file);
		if(!saveData.isEmpty()) {
			Player blackPlayer;
			Player whitePlayer;
			GAME_MODE saveDataGameMode = saveData.getGameMode();
			String blackPlayerName = saveData.getBlackPlayerName();
			String whitePlayerName = saveData.getWhitePlayerName();
			PLAYER_COLOR startColor = saveData.getStartColor();
			int numWalls = saveData.getNumWalls();
			Vector<TurnInfo> actionHistory = saveData.getHistory();
			boolean giveUp = saveData.isGiveUp();
			boolean completed = saveData.isCompleted();

			if(completed) {		// 완료된 게임일 경우
				if(gameMode == GAME_MODE.REPLAY) {
					blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, blackPlayerName, null);
					whitePlayer = new Player(PLAYER_COLOR.WHITE, numWalls, whitePlayerName, null);

					gameThread = new ReplayThread(GAME_MODE.REPLAY, blackPlayer, whitePlayer, startColor, actionHistory, giveUp);
					gameState = gameThread.getGameState();
				} else {
					JOptionPane.showMessageDialog(null, "진행중인 게임은 리플레이가 불가합니다.", "불러오기 오류", JOptionPane.OK_OPTION);
					return;
				}
			} else {	// 진행중이던 게임일 경우
				if(gameMode == GAME_MODE.LOAD_GAME) {
					queue = new ArrayBlockingQueue<GameAction>(10);

					if(saveDataGameMode == GAME_MODE.SINGLE) {
						blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, blackPlayerName, queue);
						whitePlayer = new AI(PLAYER_COLOR.WHITE, numWalls, "AI");
					} else {
						blackPlayer = new Player(PLAYER_COLOR.BLACK, numWalls, blackPlayerName, queue);
						whitePlayer = new Player(PLAYER_COLOR.WHITE, numWalls, whitePlayerName, queue);
					}

					gameThread = new LocalGameThread(saveDataGameMode, blackPlayer, whitePlayer, startColor);
					gameState = gameThread.getGameState();
					gameState.setHistory(actionHistory);
					gameState.MoveToLastTurn();
				} else {
					JOptionPane.showMessageDialog(null, "완료된 게임은 불러올 수 없습니다.", "불러오기 오류", JOptionPane.OK_OPTION);
					return;
				}
			}
			
			StartGame();
		}
	}

	public GameState getGameState() { return gameState; }
	public GameThread getGameThread() { return gameThread; }
	public String getSaveFileName() { return Utils.getDate(); }
	public boolean isLocalPlayerTurn() {
		if(gameMode == GAME_MODE.NETWORK_HOST) {
			if(gameState.getCurrentPlayer().getPlayerColor() == PLAYER_COLOR.BLACK) {
				return true;
			}
		} else if(gameMode == GAME_MODE.NETWORK_GUEST) {
			if(gameState.getCurrentPlayer().getPlayerColor() == PLAYER_COLOR.WHITE) {
				return true;
			}
		}
		return false;
	}

	private PLAYER_COLOR getRandomStartPlayer() {
		PLAYER_COLOR startColor = PLAYER_COLOR.WHITE;

		if((int)(Math.random()*2) == 0) {
			startColor = PLAYER_COLOR.BLACK;
		}

		return startColor;
	}

	private void StartGame() {
		gameThread.setDaemon(true);
		gameThread.start();
		gameFrame = new GameFrame(this, queue);
		gameFrame.runThread();
	}

	// 게임 저장
	public void Save(String fileName, boolean completed)
	{
		Utils.MakeDir(completedSaveDirectory);
		Utils.MakeDir(incompletedSaveDirectory);
		if(fileName != null) {
			try {
				if(completed == false) {
					fileName = Paths.get(incompletedSaveDirectory, fileName).toString();
				} else {
					fileName = Paths.get(completedSaveDirectory, fileName).toString();
				}
				fileName += ".data";
				MyLogger.getInstance().info("게임이 저장되었습니다. 파일 경로: " + fileName);
				FileOutputStream fos = new FileOutputStream(fileName);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				SaveData saveData = new SaveData(gameState);
				oos.writeObject(saveData);
				oos.flush();
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "저장하는 과정에서 문제가 발생하였습니다.", "저장 오류", JOptionPane.OK_OPTION);
			}
		}
	}

	public SaveData Load(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			SaveData saveData = (SaveData)ois.readObject();
			ois.close();
			return saveData;
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "불러오는 과정에서 문제가 발생하였습니다.", "불러오기 오류", JOptionPane.OK_OPTION);
			return SaveData.getEmptyData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return SaveData.getEmptyData();
		}
	}
}
