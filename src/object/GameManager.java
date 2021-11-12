package object;

import java.awt.Point;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import java.io.*;
import javax.swing.JOptionPane;

import enums.GAME_MODE;
import networking.NetWorkSocket;

/*
 * 게임의 정보를 관리하는 클래스
 */
public class GameManager {
	
	private GAME_MODE gameMode; // 첫 화면에서 선택한 게임 모드
	private NetWorkSocket socket; // 통신을 위한 변수
	
	// 플레이어
	public Player p1;
	public Player p2;
	
	private boolean turn; // turn == true일 때 p1
	private boolean putWall; // 장애물 버튼을 눌렀는지에 대한 변수
	private int turnCount = 1; // 현재까지 진행된 턴을 세는 변수
	private int selectTurn = 1;
	
	// 옵션 설정들
	private String playerName = "Player";
	private String defaultCompletedSaveDirectory = "complete_save/";
	private String defaultIncompletedSaveDirectory = "incomplete_save/";
	private int volume = 10;
	private int wallNum = 10;
	
	private String fileName = "";
	
	// 벽
	private boolean[][][] wallPoints = new boolean[2][8][8];
	// [0][][] -> 가로벽
	// [1][][] -> 세로벽
	
	private Vector<String> gameLog = new Vector<String>();
	
	
	// 싱글 또는 멀티일 때
	public GameManager(GAME_MODE gameMode) {
		LoadSetting();									// 옵션에서 설정한 세팅 저장
		MakeDir(defaultCompletedSaveDirectory);
		MakeDir(defaultIncompletedSaveDirectory);
		this.gameMode = gameMode;
		String gMode;									// 게임 모드를 저장하기 위한 스트링
		p1 = new Player(1, this, wallNum, playerName);
		System.out.println(gameMode);
		if(gameMode == GAME_MODE.SINGLE)
		{
			p2 = new AI(2, this, wallNum, "AI");		// AI 생성
			gMode = "S";								// 세이브에 저장할 게임 모드 스트링
		}
		else
		{
			p1.setPlayerName("BLACK");					// 2인 모드일 때는 설정한 이름 대신 BLACK / WHITE로 이름 설정 
			p2 = new Player(2, this, wallNum, "WHITE");
			gMode = "M";
		}
		if((int)(Math.random()*2) == 0) turn = true;	// 시작 턴을 랜덤으로 결정
		else turn = false;
		gameLog.add(new String("GMode " + gMode + " BStart " + turn + "\r\n"));	// 게임 시작 정보 저장(게임모드, 시작턴)
		fileName = GetDate();
	}
	
	// 네트워크 게임일 때
	public GameManager(GAME_MODE gamemode, NetWorkSocket socket) {
		LoadSetting();
		MakeDir(defaultCompletedSaveDirectory);
		MakeDir(defaultIncompletedSaveDirectory);
		this.socket = socket;
		this.gameMode = gamemode;
		System.out.println(gameMode);

		if(gameMode == GAME_MODE.NETWORKHOST) // 호스트일 시 턴을 정함
		{
			wallNum = 10;
			p1 = new Player(1, this, 10, playerName);
			p2 = new Player(2, this, 10, "WHITE");
			if((int)(Math.random()*2) == 0) turn = true;
			else turn = false;
			System.out.println("플레이어 이름을 보냅니다.");	
			SendData((String)playerName);				// 자신의 플레이어 이름 보내기
			String receivedData = ReceiveData();		// 상대 플레이어 이름 받기
			p2.setPlayerName(receivedData);				// 상대 플레이어 이름 설정
			System.out.println("턴 정보를 보냅니다.");
			SendData(Boolean.toString(turn)); // 정해진 턴 정보를 상대에게 보냄
		}
		else if(gameMode == GAME_MODE.NETWORKGUEST)
		{
			wallNum = 10;
			p1 = new Player(1, this, 10, "BLACK");
			p2 = new Player(2, this, 10, playerName);
			System.out.println("플레이어 이름을 받습니다.");
			String receivedData = ReceiveData();	// 상대 플레이어 이름 받기
			p1.setPlayerName(receivedData);			// 상대 플레이어 이름 설정
			SendData(playerName);					// 자신의 플레이어 이름 보내기
			System.out.println("턴 정보를 받습니다.");
			receivedData = ReceiveData(); // 턴 정보를 받음
			if(receivedData.equals("true"))
			{
				turn = true;
			}
			else if(receivedData.equals("false"))
				turn = false;
			else
			{
				System.out.println(receivedData);
				
				System.out.println("네트워크 오류...\n프로그램을 종료합니다.");
				System.exit(0);
			}
		}
		putWall = false;
		gameLog.add(new String("GMode " + "N" + " BStart " + turn + "\r\n"));	// 게임 시작 정보 저장
		fileName = GetDate();
	}
	
	// 게임 로드 또는 리플레이 시
	public GameManager(GAME_MODE gamemode, File file) {
		MakeDir(defaultCompletedSaveDirectory);
		MakeDir(defaultIncompletedSaveDirectory);
		this.gameMode = gamemode;
		FileReader fin = null;
		//fileName = file.getName();
		try {
			fin = new FileReader(file);
			int c;
			String temp = "";
			while((c = fin.read()) != -1) {
				if(c == '\r')
				{
					c = fin.read();
					c = fin.read();
					gameLog.add(temp + "\r\n");	// 게임 로그에 파일에서 불러온 정보 저장
					temp = "";
				}
				temp += (char)c;
				turnCount++;
			}
			Iterator<String> it = gameLog.iterator();
			String turnInfo = it.next();
			System.out.println(turnInfo);
			if(gameMode == GAME_MODE.LOADGAME)					// 게임을 로드한 경우(리플레이가 아닐 경우)
			{
				if(turnInfo.substring(6, 7).equals("S"))		// 싱글 모드일 경우
					gameMode = GAME_MODE.SINGLE;
				else if(turnInfo.substring(6, 7).equals("M"))	// 멀티 모드일 경우
					gameMode = GAME_MODE.MULTY;
				fileName = file.getName().substring(0, file.getName().length()-4);
				System.out.println(fileName);
			}
			if(turnInfo.substring(15, 19).equals("true"))		// 턴 정보 불러오기
				turn = true;
			else
				turn = false;
			
			p1 = new Player(1, this, wallNum, playerName);
			if(gameMode == GAME_MODE.REPLAY)					// 리플레이하는 경우
			{
				p1.setPlayerName("BLACK");
				p2 = new Player(2, this, wallNum, "WHITE");
			} else if(gameMode == GAME_MODE.SINGLE)				// 싱글 모드일 경우
			{
				p2 = new AI(2, this, wallNum, "AI");
			}
			else												// 멀티 모드일 경우
			{
				p1.setPlayerName("BLACK");
				p2 = new Player(2, this, wallNum, "WHITE");
			}
			if(gameMode != GAME_MODE.REPLAY)					// 게임을 로드한 경우(리플레이가 아닐 경우)
			{
				while(it.hasNext())
				{
					turnInfo = it.next();
					if(turnInfo.substring(6, 10).equals("Wall"))
					{
						int wallNum = Integer.valueOf(turnInfo.substring(11, 14));
						if(turnInfo.substring(0, 5).equals("Black"))
							PutWallNum(wallNum, true);
						else
							PutWallNum(wallNum, false);
					}
					else if(turnInfo.substring(6, 10).equals("Move"))
					{
						int moveX = Integer.valueOf(turnInfo.substring(11,12));
						int moveY = Integer.valueOf(turnInfo.substring(13,14));
						int fromX = Integer.valueOf(turnInfo.substring(20,21));
						int fromY = Integer.valueOf(turnInfo.substring(22,23));
						if(turnInfo.substring(0, 5).equals("Black"))
							p1.movePlayer(new Point(moveX, moveY));
						else
							p2.movePlayer(new Point(moveX, moveY));
					}
					turn = !turn;
				}
			}
			if(gameMode == GAME_MODE.REPLAY)					// 리플레이일 경우 시작 턴을 1로 정함
				selectTurn = 1;
			else												// 게임을 로드한 경우 시작 턴을 진행된 마지막 턴으로 정함
				selectTurn = gameLog.size();
			turnCount = gameLog.size();
			
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "파일을 불러오는 과정에서 문제가 발생하였습니다.", "로드 오류", JOptionPane.OK_OPTION);
		}
	}
	
	// 벽의 위치를 반환하는 함수
	public boolean[][][] GetWallPoint() { return wallPoints; }
	// 게임 모드를 반환
	public GAME_MODE GetGameMode() { return gameMode; }
	// 턴 정보를 반환
	public boolean GetTurn() { return turn; }
	// 현재 턴 수를 반환(이전버튼, 다음버튼에 필요)
	public int GetSelectTurn() { return selectTurn; }
	// 현재까지 진행된 턴 수를 반환
	public int GetTurnCount() { return turnCount; }
	// 턴을 바꾸는 함수
	public void ChangeTurn() { turn = !turn; }
	// 이전버튼을 누른 후 다른 위치로 이동 시, 뒤의 벡터를 지우는 함수
	public void SetTurnCount() {
		System.out.println("TC, ST : " + turnCount + " " + selectTurn);
		System.out.println(gameLog.size());
		int glSize = gameLog.size();				// for문에 사용하기 위해 게임 로그 사이즈 저장
		for(int i = selectTurn; i < glSize; i++) {
			gameLog.remove(selectTurn);				// 현재 턴 다음의 모든 기록 삭제
			System.out.println("removed " + i);
		}
		for(int i = 0; i < gameLog.size(); i++)
			System.out.print(i + " " + gameLog.get(i));
		this.turnCount = selectTurn;
		System.out.print(gameLog.get(gameLog.size()-1));
		System.out.println("selectTurn : " + selectTurn);
		System.out.println(turnCount + " " + selectTurn);
		System.out.println();
	}
	// 다음 턴
	public void NextTurn() {
		turnCount++;
		selectTurn++;
		ChangeTurn();
		putWall = false;
	}
	// 이전 턴 정보 반환
	public String GetPrevTurn() {
		turn = !turn;
		selectTurn --;
		System.out.println(turnCount + " " + selectTurn);
		return gameLog.elementAt(selectTurn);
	}
	// 다음 턴 정보 반환
	public String GetNextTurn() {
		turn = !turn;
		return gameLog.elementAt(selectTurn++);
	}
	// 볼륨 반환
	public int GetVolume() { return volume; }
	// 파일명 불러오기
	public String GetFileName() { return fileName; }
	// 장애물 버튼을 눌렀었는지를 반환
	public boolean GetPutMode() { return putWall; }
	// 장애물 모드, 이동 모드를 전환하는 함수
	public void ChangePutMode() { putWall = !putWall; }
	// 소켓을 반환하는 함수
	public NetWorkSocket GetSocket() { return socket; }
	
	// 상대에게 데이터 전송
	public void SendData(String message) {
		socket.SendData(message);
	}
	
	// 데이터 받아오기
	public String ReceiveData() {
		return socket.ReceiveData();
	}
	
	// 세팅을 불러오는 함수
	public void LoadSetting() {
		try {
			FileReader fin = new FileReader("option/setting.txt");	// 세팅이 저장된 텍스트 파일
			
			int c, i = 0;
			String[] options = new String[5];
			String temp = "";
			while((c = fin.read()) != -1)
			{
				if(c == '\r')
				{
					c = fin.read();
					c = fin.read();
					options[i] = temp;
					i++;
					temp = "";
				}
				temp += (char)c;
			}
			
			playerName = options[0];								// 세팅을 불러옴
			defaultCompletedSaveDirectory = options[1];
			defaultIncompletedSaveDirectory = options[2];
			volume = Integer.parseInt(options[3]);
			wallNum = Integer.parseInt(options[4]);		
			fin.close();
		}
		catch (IOException e) {
			
		}
	}
	
	// 이동 가능한 위치를 포인트로 반환하는 함수
	public Point[] GetAvailablePoint() {
		Point[] availablePoints; // 크기 4의 배열. 오른쪽, 왼쪽, 아래쪽, 위쪽 순
		
		if(turn == true) {
			availablePoints = p1.updateAvailablePlace(wallPoints, p2.getPoint());
		}
		else {
			availablePoints = p2.updateAvailablePlace(wallPoints, p1.getPoint());
		}
		
		return availablePoints;
	}
	
	// 벽을 놓는 함수
	public boolean PutWallNum(int wallNum, boolean BTurn) {	// wallNum은 세 자리 숫자
		boolean isVertical;
		int vWallNum = wallNum%10;	// 일의 자리 숫자는 가로의 위치
		int hWallNum = (wallNum/10)%10;	// 십의 자리 숫자는 세로의 위치

		vWallNum = (vWallNum > 7) ? 7 : vWallNum;
		hWallNum = (hWallNum > 7) ? 7 : hWallNum;
		System.out.println(vWallNum + ", " + hWallNum + " " + wallNum);
		
		if(wallNum / 100 == 1)	// 백의 자리 숫자는 가로벽인지 세로벽인지를 결정
			isVertical = true;
		else isVertical = false;
		
		if(isVertical == true)
		{
			if(!wallPoints[0][vWallNum][hWallNum] && !wallPoints[0][(vWallNum-1) < 0 ? 0 : (vWallNum-1)][hWallNum] &&
					!wallPoints[0][(vWallNum+1) > 7 ? 7 : (vWallNum+1)][hWallNum] && // 양 옆으로 겹치게 세울 수 없는 벽
					!wallPoints[1][vWallNum][hWallNum]) // 세로로 겹치게 세울 수 없는 벽
			{
				wallPoints[0][vWallNum][hWallNum] = true;
				if(BTurn == true)
					p1.minusWallNum(); // 놓을 수 있는 벽의 수 감소
				else
					p2.minusWallNum();
				return true;
			}
		}
		else
		{
			if(!wallPoints[1][vWallNum][hWallNum] && !wallPoints[1][vWallNum][(hWallNum-1) < 0 ? 0 : (hWallNum-1)] &&
					!wallPoints[1][vWallNum][(hWallNum+1) > 7 ? 7 : (hWallNum+1)] && // 위아래로 겹치게 세울 수 없는 벽
					!wallPoints[0][vWallNum][hWallNum]) // 가로로 겹치게 세울 수 없는 벽
			{
				wallPoints[1][vWallNum][hWallNum] = true;
				if(BTurn == true)
					p1.minusWallNum();
				else
					p2.minusWallNum();
				return true;
			}
		}
		return false;
	}
	
	// 벽을 제거하는 함수
	public void DeleteWallNum(int wallNum, boolean BTurn) {
		boolean isVertical;
		int vWallNum = wallNum%10;	// 일의 자리 숫자는 가로의 위치
		int hWallNum = (wallNum/10)%10;	// 십의 자리 숫자는 세로의 위치

		vWallNum = (vWallNum > 7) ? 7 : vWallNum;
		hWallNum = (hWallNum > 7) ? 7 : hWallNum;
		System.out.println(vWallNum + ", " + hWallNum + " " + wallNum);
		
		if(wallNum / 100 == 1)	// 백의 자리 숫자는 가로벽인지 세로벽인지를 결정
			isVertical = true;
		else isVertical = false;
		
		if(isVertical == true)
		{
			wallPoints[0][vWallNum][hWallNum] = false;
			if(BTurn == true)
				p1.plusWallNum();	// 놓을 수 있는 벽의 수 증가
			else
				p2.plusWallNum();	// 놓을 수 있는 벽의 수 증가
		}
		else
		{
			wallPoints[1][vWallNum][hWallNum] = false;
			if(BTurn == true)
				p1.plusWallNum();	// 놓을 수 있는 벽의 수 증가
			else
				p2.plusWallNum();	// 놓을 수 있는 벽의 수 증가
		}
	}

	// 게임 오버인지 검사
	public boolean GameOver() { 
		if(p1.getPoint().y == 0 || p2.getPoint().y == 8)
		{
			return true;
		}
		return false;
	}
	
	// 게임 정보 기록
	public void Record(String log)
	{
		if(turn == false)
			gameLog.add("Black " + log + "\r\n");
		else
			gameLog.add("White " + log + "\r\n");
	}
	
	// 게임 저장
	public void Save(String fileName, boolean completed)
	{
		FileWriter fout = null;
		if(fileName != null)
			try {
				if(completed == false)
					fout = new FileWriter("incomplete_save/" + fileName + ".txt");
				else
					fout = new FileWriter("complete_save/" + fileName + ".txt");
				for(int i = 0; i < gameLog.size(); i++)
					fout.write(gameLog.get(i));
				fout.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "저장하는 과정에서 문제가 발생하였습니다.", "저장 오류", JOptionPane.OK_OPTION);
			}
	}
	
	// 폴더 생성
	public void MakeDir(String dirName)
	{
		File Folder = new File(dirName);
		if(!Folder.exists()) {
			Folder.mkdir();
		}
	}

	public String GetDate()
	{
		Calendar now = Calendar.getInstance();
		int yy = now.get(Calendar.YEAR);
		int mm = now.get(Calendar.MONTH);
		int dd = now.get(Calendar.DAY_OF_MONTH);
		int hh = now.get(Calendar.HOUR_OF_DAY);
		int MM = now.get(Calendar.MINUTE);
		int ss = now.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer();
		sb.append(yy);
		sb.append("-");
		sb.append(mm + 1);
		sb.append("-");
		sb.append(dd);
		sb.append(" ");
		sb.append(hh);
		sb.append("_");
		sb.append(MM);
		sb.append("_");
		sb.append(ss);
		String nowDate = sb.toString();
		
		return nowDate;
	}


}
