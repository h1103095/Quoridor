package networking;

import javax.swing.*;

/* 
 * 호스트와 클라이언트의 상위 클래스
 */
public abstract class NetWorkSocket {
	public abstract void SendData(String msg);		// 데이터 보내기
	public abstract String ReceiveData();			// 데이터 받기
	public abstract void CloseSocket();				// 소켓 닫기
	public abstract boolean OpenServer();			// 서버 열기
	public abstract boolean JoinServer(String HostIP, JTextArea connectLogTA, JButton closeBT);	// 서버에 들어가기
	public abstract String GetHostIP();				// 호스트 IP 얻기
}
