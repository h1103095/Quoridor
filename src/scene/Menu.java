package scene;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.*;

import object.GameManager;
import enums.GAME_MODE;

/*
 * �� ó�� ���� ���� �� ��Ÿ���� �����Ӱ� �� �������
 */



// �޴� ������
public class Menu extends JFrame{
	public Menu() {
		Point appSize = new Point(1000, 600);	// â ������
		Point menuSize = new Point(400, 400);	// ��ư �κ� ������
		
		setTitle("Quoridor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		BackgroundPanel bp = new BackgroundPanel();
		bp.setSize(appSize.x, appSize.y);
		bp.setLayout(null);
		
		MenuPanel mp = new MenuPanel(this);
		mp.setLayout(new GridLayout(8, 1, 0, 3));
		mp.setLocation((appSize.x - menuSize.x)/2, (appSize.y - menuSize.y)/2);	// ��ư�� ��ġ�� â�� �߾����� ����
		mp.setSize(menuSize.x, menuSize.y);
		
		contentPane.setLayout(null);
		bp.add(mp);
		contentPane.add(bp);

		setSize(appSize.x, appSize.y);
		setVisible(true);
		
		mp.requestFocus();
	}


	// ��ư �г�
	class MenuPanel extends JPanel {
		String[] menuLabels = {
				"1�ο� ����", "2�ο� ����", "2�ο� ��Ʈ��ũ ���� (���ӻ���)", "2�ο� ��Ʈ��ũ ���� (��������)",
				"�������� Load", "�������� ����", "ȯ�漳��", "����"
		};
		ImageIcon[] buttonIcons = {
				new ImageIcon("images/SINGLE.png"), new ImageIcon("images/MULTY.png"), new ImageIcon("images/NetWork.jpg"),
				new ImageIcon("images/NetWork.jpg"), new ImageIcon("images/Load.jpg"), new ImageIcon("images/Replay.png"),
				new ImageIcon("images/Option.jpg"), new ImageIcon("images/Exit.png")
		};
		JButton[] menuButtons = new JButton[8];
		GameManager GM;
		String defaultCompletedSaveDirectory = "complete_save/";
		String defaultIncompletedSaveDirectory = "incomplete_save/";
		private Frame Menu;
		public MenuPanel(Frame menu) {
			this.Menu = menu; 
			BtnListener listener = new BtnListener();
			for(int i = 0; i < 8; i++) {
				menuButtons[i] = new JButton(menuLabels[i], buttonIcons[i]);
				menuButtons[i].addActionListener(listener);
				add(menuButtons[i]);
			}
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == menuButtons[0]) { // 1�ο� ����
					GM = new GameManager(GAME_MODE.SINGLE);
					new Game(GM);
					Menu.dispose(); // �޴� ����
				}
				else if(e.getSource() == menuButtons[1]) { // 2�ο� ����
					GM = new GameManager(GAME_MODE.MULTY);
					new Game(GM);
					Menu.dispose();
				}else if(e.getSource()==menuButtons[2]) { // ��Ʈ��ũ ȣ��Ʈ
					new ServerFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[3]) { // ��Ʈ��ũ �Խ�Ʈ
					new ClientFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[4]) { // ���� �ε�
					JFileChooser fc = new JFileChooser();
					File file;
					LoadSetting();
					fc.setCurrentDirectory(new File(defaultIncompletedSaveDirectory));	// ���̺� ���
					fc.setFileFilter(new FileNameExtensionFilter("TXT File", "txt"));	// �ؽ�Ʈ ���ϸ� ���̵��� ����
					
					int returnVal = fc.showOpenDialog(Menu.this);						// ���� ���� â ����
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						GM = new GameManager(GAME_MODE.LOADGAME, file);
						new Game(GM);
						System.out.println("Opening: " + file.getName() + ".");
						Menu.dispose();
					}
				}
				else if(e.getSource()==menuButtons[5]) { // ���÷���
					JFileChooser fc = new JFileChooser();
					LoadSetting();
					fc.setCurrentDirectory(new File(defaultCompletedSaveDirectory));	// ����� ���� ���̺� ���
					fc.setFileFilter(new FileNameExtensionFilter("TXT File", "txt"));
					
					int returnVal = fc.showOpenDialog(Menu.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						GM = new GameManager(GAME_MODE.REPLAY, file);
						new Replay(GM);
						System.out.println("Opening: " + file.getName() + ".");
						Menu.dispose();
					}
				}
				else if(e.getSource()==menuButtons[6]) { // ȯ�漳��
					new OptionFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[7]) { // ����
					System.exit(0);
				}
			}
		}
		public void LoadSetting() {
			try {
				FileReader fin = new FileReader("option/setting.txt");
				
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
				defaultCompletedSaveDirectory = options[1];
				defaultIncompletedSaveDirectory = options[2];		
				fin.close();
			}
			catch (IOException e) {
				
			}
		}
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Menu();
	}

}