package networking;

import java.awt.*;
import javax.swing.*;

import object.GameManager;
import enums.GAME_MODE;
import scene.Game;

/*
 * 서버와 클라이언트를 담는 쓰레드
 */

public class SocketThread extends Thread {
	private NetWorkSocket socket;	// 서버 또는 클라이언트
	private Frame frame;			// 창 종료를 위한 객체
	private String hostIP;			// 호스트 IP
	private JTextArea connectLog;	// 통신 기록
	private JButton closeBT;		// 취소 버튼
	public SocketThread(NetWorkSocket socket, Frame frame) {
		this.socket = socket;
		this.frame = frame;
	}
	public SocketThread(NetWorkSocket socket, Frame frame, String HostIP, JTextArea connectLogTA, JButton closeBT) {
		this.socket = socket;
		this.frame = frame;
		this.hostIP = HostIP;
		this.connectLog = connectLogTA;
		this.closeBT = closeBT;
	}
	public void run() {
		// 호스트일 시
		if(socket.toString() == "Host" && socket.OpenServer())
		{
			GameManager GM = new GameManager(GAME_MODE.NETWORKHOST, getSocket());
			new Game(GM);		// 네트워크 호스트로 게임 생성
			frame.dispose();	// 연결 창 닫기
		}
		// 클라이언트일 시
		else if(socket.toString() == "Client" && socket.JoinServer(hostIP, connectLog, closeBT))
		{
			GameManager GM = new GameManager(GAME_MODE.NETWORKGUEST, getSocket());
			new Game(GM);		// 네트워크 게스트로 게임 생성
			frame.dispose();	// 연결 창 닫기
		}
	}
	// 소켓 반환
	public NetWorkSocket getSocket() { return socket; }
}