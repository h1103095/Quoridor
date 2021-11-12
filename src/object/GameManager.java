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
 * ������ ������ �����ϴ� Ŭ����
 */
public class GameManager {
	
	private GAME_MODE gameMode; // ù ȭ�鿡�� ������ ���� ���
	private NetWorkSocket socket; // ����� ���� ����
	
	// �÷��̾�
	public Player p1;
	public Player p2;
	
	private boolean turn; // turn == true�� �� p1
	private boolean putWall; // ��ֹ� ��ư�� ���������� ���� ����
	private int turnCount = 1; // ������� ����� ���� ���� ����
	private int selectTurn = 1;
	
	// �ɼ� ������
	private String playerName = "Player";
	private String defaultCompletedSaveDirectory = "complete_save/";
	private String defaultIncompletedSaveDirectory = "incomplete_save/";
	private int volume = 10;
	private int wallNum = 10;
	
	private String fileName = "";
	
	// ��
	private boolean[][][] wallPoints = new boolean[2][8][8];
	// [0][][] -> ���κ�
	// [1][][] -> ���κ�
	
	private Vector<String> gameLog = new Vector<String>();
	
	
	// �̱� �Ǵ� ��Ƽ�� ��
	public GameManager(GAME_MODE gameMode) {
		LoadSetting();									// �ɼǿ��� ������ ���� ����
		MakeDir(defaultCompletedSaveDirectory);
		MakeDir(defaultIncompletedSaveDirectory);
		this.gameMode = gameMode;
		String gMode;									// ���� ��带 �����ϱ� ���� ��Ʈ��
		p1 = new Player(1, this, wallNum, playerName);
		System.out.println(gameMode);
		if(gameMode == GAME_MODE.SINGLE)
		{
			p2 = new AI(2, this, wallNum, "AI");		// AI ����
			gMode = "S";								// ���̺꿡 ������ ���� ��� ��Ʈ��
		}
		else
		{
			p1.setPlayerName("BLACK");					// 2�� ����� ���� ������ �̸� ��� BLACK / WHITE�� �̸� ���� 
			p2 = new Player(2, this, wallNum, "WHITE");
			gMode = "M";
		}
		if((int)(Math.random()*2) == 0) turn = true;	// ���� ���� �������� ����
		else turn = false;
		gameLog.add(new String("GMode " + gMode + " BStart " + turn + "\r\n"));	// ���� ���� ���� ����(���Ӹ��, ������)
		fileName = GetDate();
	}
	
	// ��Ʈ��ũ ������ ��
	public GameManager(GAME_MODE gamemode, NetWorkSocket socket) {
		LoadSetting();
		MakeDir(defaultCompletedSaveDirectory);
		MakeDir(defaultIncompletedSaveDirectory);
		this.socket = socket;
		this.gameMode = gamemode;
		System.out.println(gameMode);

		if(gameMode == GAME_MODE.NETWORKHOST) // ȣ��Ʈ�� �� ���� ����
		{
			wallNum = 10;
			p1 = new Player(1, this, 10, playerName);
			p2 = new Player(2, this, 10, "WHITE");
			if((int)(Math.random()*2) == 0) turn = true;
			else turn = false;
			System.out.println("�÷��̾� �̸��� �����ϴ�.");	
			SendData((String)playerName);				// �ڽ��� �÷��̾� �̸� ������
			String receivedData = ReceiveData();		// ��� �÷��̾� �̸� �ޱ�
			p2.setPlayerName(receivedData);				// ��� �÷��̾� �̸� ����
			System.out.println("�� ������ �����ϴ�.");
			SendData(Boolean.toString(turn)); // ������ �� ������ ��뿡�� ����
		}
		else if(gameMode == GAME_MODE.NETWORKGUEST)
		{
			wallNum = 10;
			p1 = new Player(1, this, 10, "BLACK");
			p2 = new Player(2, this, 10, playerName);
			System.out.println("�÷��̾� �̸��� �޽��ϴ�.");
			String receivedData = ReceiveData();	// ��� �÷��̾� �̸� �ޱ�
			p1.setPlayerName(receivedData);			// ��� �÷��̾� �̸� ����
			SendData(playerName);					// �ڽ��� �÷��̾� �̸� ������
			System.out.println("�� ������ �޽��ϴ�.");
			receivedData = ReceiveData(); // �� ������ ����
			if(receivedData.equals("true"))
			{
				turn = true;
			}
			else if(receivedData.equals("false"))
				turn = false;
			else
			{
				System.out.println(receivedData);
				
				System.out.println("��Ʈ��ũ ����...\n���α׷��� �����մϴ�.");
				System.exit(0);
			}
		}
		putWall = false;
		gameLog.add(new String("GMode " + "N" + " BStart " + turn + "\r\n"));	// ���� ���� ���� ����
		fileName = GetDate();
	}
	
	// ���� �ε� �Ǵ� ���÷��� ��
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
					gameLog.add(temp + "\r\n");	// ���� �α׿� ���Ͽ��� �ҷ��� ���� ����
					temp = "";
				}
				temp += (char)c;
				turnCount++;
			}
			Iterator<String> it = gameLog.iterator();
			String turnInfo = it.next();
			System.out.println(turnInfo);
			if(gameMode == GAME_MODE.LOADGAME)					// ������ �ε��� ���(���÷��̰� �ƴ� ���)
			{
				if(turnInfo.substring(6, 7).equals("S"))		// �̱� ����� ���
					gameMode = GAME_MODE.SINGLE;
				else if(turnInfo.substring(6, 7).equals("M"))	// ��Ƽ ����� ���
					gameMode = GAME_MODE.MULTY;
				fileName = file.getName().substring(0, file.getName().length()-4);
				System.out.println(fileName);
			}
			if(turnInfo.substring(15, 19).equals("true"))		// �� ���� �ҷ�����
				turn = true;
			else
				turn = false;
			
			p1 = new Player(1, this, wallNum, playerName);
			if(gameMode == GAME_MODE.REPLAY)					// ���÷����ϴ� ���
			{
				p1.setPlayerName("BLACK");
				p2 = new Player(2, this, wallNum, "WHITE");
			} else if(gameMode == GAME_MODE.SINGLE)				// �̱� ����� ���
			{
				p2 = new AI(2, this, wallNum, "AI");
			}
			else												// ��Ƽ ����� ���
			{
				p1.setPlayerName("BLACK");
				p2 = new Player(2, this, wallNum, "WHITE");
			}
			if(gameMode != GAME_MODE.REPLAY)					// ������ �ε��� ���(���÷��̰� �ƴ� ���)
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
			if(gameMode == GAME_MODE.REPLAY)					// ���÷����� ��� ���� ���� 1�� ����
				selectTurn = 1;
			else												// ������ �ε��� ��� ���� ���� ����� ������ ������ ����
				selectTurn = gameLog.size();
			turnCount = gameLog.size();
			
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "������ �ҷ����� �������� ������ �߻��Ͽ����ϴ�.", "�ε� ����", JOptionPane.OK_OPTION);
		}
	}
	
	// ���� ��ġ�� ��ȯ�ϴ� �Լ�
	public boolean[][][] GetWallPoint() { return wallPoints; }
	// ���� ��带 ��ȯ
	public GAME_MODE GetGameMode() { return gameMode; }
	// �� ������ ��ȯ
	public boolean GetTurn() { return turn; }
	// ���� �� ���� ��ȯ(������ư, ������ư�� �ʿ�)
	public int GetSelectTurn() { return selectTurn; }
	// ������� ����� �� ���� ��ȯ
	public int GetTurnCount() { return turnCount; }
	// ���� �ٲٴ� �Լ�
	public void ChangeTurn() { turn = !turn; }
	// ������ư�� ���� �� �ٸ� ��ġ�� �̵� ��, ���� ���͸� ����� �Լ�
	public void SetTurnCount() {
		System.out.println("TC, ST : " + turnCount + " " + selectTurn);
		System.out.println(gameLog.size());
		int glSize = gameLog.size();				// for���� ����ϱ� ���� ���� �α� ������ ����
		for(int i = selectTurn; i < glSize; i++) {
			gameLog.remove(selectTurn);				// ���� �� ������ ��� ��� ����
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
	// ���� ��
	public void NextTurn() {
		turnCount++;
		selectTurn++;
		ChangeTurn();
		putWall = false;
	}
	// ���� �� ���� ��ȯ
	public String GetPrevTurn() {
		turn = !turn;
		selectTurn --;
		System.out.println(turnCount + " " + selectTurn);
		return gameLog.elementAt(selectTurn);
	}
	// ���� �� ���� ��ȯ
	public String GetNextTurn() {
		turn = !turn;
		return gameLog.elementAt(selectTurn++);
	}
	// ���� ��ȯ
	public int GetVolume() { return volume; }
	// ���ϸ� �ҷ�����
	public String GetFileName() { return fileName; }
	// ��ֹ� ��ư�� ������������ ��ȯ
	public boolean GetPutMode() { return putWall; }
	// ��ֹ� ���, �̵� ��带 ��ȯ�ϴ� �Լ�
	public void ChangePutMode() { putWall = !putWall; }
	// ������ ��ȯ�ϴ� �Լ�
	public NetWorkSocket GetSocket() { return socket; }
	
	// ��뿡�� ������ ����
	public void SendData(String message) {
		socket.SendData(message);
	}
	
	// ������ �޾ƿ���
	public String ReceiveData() {
		return socket.ReceiveData();
	}
	
	// ������ �ҷ����� �Լ�
	public void LoadSetting() {
		try {
			FileReader fin = new FileReader("option/setting.txt");	// ������ ����� �ؽ�Ʈ ����
			
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
			
			playerName = options[0];								// ������ �ҷ���
			defaultCompletedSaveDirectory = options[1];
			defaultIncompletedSaveDirectory = options[2];
			volume = Integer.parseInt(options[3]);
			wallNum = Integer.parseInt(options[4]);		
			fin.close();
		}
		catch (IOException e) {
			
		}
	}
	
	// �̵� ������ ��ġ�� ����Ʈ�� ��ȯ�ϴ� �Լ�
	public Point[] GetAvailablePoint() {
		Point[] availablePoints; // ũ�� 4�� �迭. ������, ����, �Ʒ���, ���� ��
		
		if(turn == true) {
			availablePoints = p1.updateAvailablePlace(wallPoints, p2.getPoint());
		}
		else {
			availablePoints = p2.updateAvailablePlace(wallPoints, p1.getPoint());
		}
		
		return availablePoints;
	}
	
	// ���� ���� �Լ�
	public boolean PutWallNum(int wallNum, boolean BTurn) {	// wallNum�� �� �ڸ� ����
		boolean isVertical;
		int vWallNum = wallNum%10;	// ���� �ڸ� ���ڴ� ������ ��ġ
		int hWallNum = (wallNum/10)%10;	// ���� �ڸ� ���ڴ� ������ ��ġ

		vWallNum = (vWallNum > 7) ? 7 : vWallNum;
		hWallNum = (hWallNum > 7) ? 7 : hWallNum;
		System.out.println(vWallNum + ", " + hWallNum + " " + wallNum);
		
		if(wallNum / 100 == 1)	// ���� �ڸ� ���ڴ� ���κ����� ���κ������� ����
			isVertical = true;
		else isVertical = false;
		
		if(isVertical == true)
		{
			if(!wallPoints[0][vWallNum][hWallNum] && !wallPoints[0][(vWallNum-1) < 0 ? 0 : (vWallNum-1)][hWallNum] &&
					!wallPoints[0][(vWallNum+1) > 7 ? 7 : (vWallNum+1)][hWallNum] && // �� ������ ��ġ�� ���� �� ���� ��
					!wallPoints[1][vWallNum][hWallNum]) // ���η� ��ġ�� ���� �� ���� ��
			{
				wallPoints[0][vWallNum][hWallNum] = true;
				if(BTurn == true)
					p1.minusWallNum(); // ���� �� �ִ� ���� �� ����
				else
					p2.minusWallNum();
				return true;
			}
		}
		else
		{
			if(!wallPoints[1][vWallNum][hWallNum] && !wallPoints[1][vWallNum][(hWallNum-1) < 0 ? 0 : (hWallNum-1)] &&
					!wallPoints[1][vWallNum][(hWallNum+1) > 7 ? 7 : (hWallNum+1)] && // ���Ʒ��� ��ġ�� ���� �� ���� ��
					!wallPoints[0][vWallNum][hWallNum]) // ���η� ��ġ�� ���� �� ���� ��
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
	
	// ���� �����ϴ� �Լ�
	public void DeleteWallNum(int wallNum, boolean BTurn) {
		boolean isVertical;
		int vWallNum = wallNum%10;	// ���� �ڸ� ���ڴ� ������ ��ġ
		int hWallNum = (wallNum/10)%10;	// ���� �ڸ� ���ڴ� ������ ��ġ

		vWallNum = (vWallNum > 7) ? 7 : vWallNum;
		hWallNum = (hWallNum > 7) ? 7 : hWallNum;
		System.out.println(vWallNum + ", " + hWallNum + " " + wallNum);
		
		if(wallNum / 100 == 1)	// ���� �ڸ� ���ڴ� ���κ����� ���κ������� ����
			isVertical = true;
		else isVertical = false;
		
		if(isVertical == true)
		{
			wallPoints[0][vWallNum][hWallNum] = false;
			if(BTurn == true)
				p1.plusWallNum();	// ���� �� �ִ� ���� �� ����
			else
				p2.plusWallNum();	// ���� �� �ִ� ���� �� ����
		}
		else
		{
			wallPoints[1][vWallNum][hWallNum] = false;
			if(BTurn == true)
				p1.plusWallNum();	// ���� �� �ִ� ���� �� ����
			else
				p2.plusWallNum();	// ���� �� �ִ� ���� �� ����
		}
	}

	// ���� �������� �˻�
	public boolean GameOver() { 
		if(p1.getPoint().y == 0 || p2.getPoint().y == 8)
		{
			return true;
		}
		return false;
	}
	
	// ���� ���� ���
	public void Record(String log)
	{
		if(turn == false)
			gameLog.add("Black " + log + "\r\n");
		else
			gameLog.add("White " + log + "\r\n");
	}
	
	// ���� ����
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
				JOptionPane.showMessageDialog(null, "�����ϴ� �������� ������ �߻��Ͽ����ϴ�.", "���� ����", JOptionPane.OK_OPTION);
			}
	}
	
	// ���� ����
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
