package scene;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/*
 * ���� �ɼ��� �����ϴ� â
 */

public class OptionFrame extends JFrame{
	
	OptionFrame() {
		setTitle("ȯ�漳��");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		Point appSize = new Point(1000, 600);
		Point menuSize = new Point(400, 200);
		
		OptionPanel op = new OptionPanel(this);
		op.setLayout(new GridLayout(7, 2, 0, 3));
		op.setLocation((appSize.x - menuSize.x)/2, (appSize.y - menuSize.y)/2);
		op.setSize(menuSize.x, menuSize.y);
		
		BackgroundPanel bp = new BackgroundPanel();
		bp.setSize(appSize.x, appSize.y);
		bp.setLayout(null);
		
		bp.add(op);
		contentPane.add(bp);
		
		setSize(appSize.x, appSize.y);
		setVisible(true);
	}
	
	class OptionPanel extends JPanel {
		String[] optionString = {"�̸� ����", "���� ���丮 ����", "����� ���� ���丮 ����", "ȿ���� ũ��", "��ֹ� ����"};
		JLabel[] optionLabels = new JLabel[5];
		String[] defaultOptions = {"Player", "complete_save/", "incomplete_save/", "10", "10"};
				
		JTextField[] optionTexts = new JTextField[6];
		JButton ClearCompletedSaveFiles = new JButton("�Ϸ�� ���� ��� ����");
		JButton ClearIncompletedSaveFiles = new JButton("�������� ���� ��� ����");
		JButton OKbutton = new JButton("����");
		JButton CANCELbutton = new JButton("�ݱ�");
		
		private Frame optionFrame;
		
		OptionPanel(Frame optionFrame) {
			this.optionFrame = optionFrame;
			BtnListener listener = new BtnListener();
			for(int i = 0; i < 5; i++) {
				LoadSetting();
				optionLabels[i] = new JLabel(optionString[i]);
				add(optionLabels[i]);
				optionTexts[i] = new JTextField(defaultOptions[i], 20);
				add(optionTexts[i]);
			}
			ClearCompletedSaveFiles.addActionListener(listener);
			ClearIncompletedSaveFiles.addActionListener(listener);
			OKbutton.addActionListener(listener);
			CANCELbutton.addActionListener(listener);
			add(ClearCompletedSaveFiles);
			add(ClearIncompletedSaveFiles);
			add(OKbutton);
			add(CANCELbutton);
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == ClearCompletedSaveFiles) {
					int result = JOptionPane.showConfirmDialog(null, "���� ���̺� ������ �����Ͻðڽ��ϱ�?", "Confirm", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(true);
						JOptionPane.showMessageDialog(null, "�����Ǿ����ϴ�.", "���� �Ϸ�", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
					}
				}
				else if(e.getSource() == ClearIncompletedSaveFiles) {
					int result = JOptionPane.showConfirmDialog(null, "���� ���̺� ������ �����Ͻðڽ��ϱ�?", "Confirm", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(false);
						JOptionPane.showMessageDialog(null, "�����Ǿ����ϴ�.", "���� �Ϸ�", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
					}
				}
				else if(e.getSource() == OKbutton) {
					if(Integer.parseInt(optionTexts[3].getText()) >= 0 && Integer.parseInt(optionTexts[3].getText()) <= 20) 
						SaveSetting();			// �ɼ� ����
					else
						JOptionPane.showMessageDialog(null, "�������� 0���� 20 ���̷� �����ּ���.", "���� ����", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
				}
				else if(e.getSource() == CANCELbutton) {
					new Menu();
					optionFrame.dispose();	// �ɼ� ������
				}
			}
		}
		
		// �ɼ� ����
		public void SaveSetting() {
			FileWriter fout = null;
			
			for(int i = 0; i < 5; i++)
			{
				defaultOptions[i] = optionTexts[i].getText();
			}
			
			try {
				fout = new FileWriter("option/setting.txt");
		
				for(int i = 0; i < 5; i++)
					fout.write(defaultOptions[i] + "\r\n");
				JOptionPane.showMessageDialog(null, "����Ǿ����ϴ�.", "�˸�", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
				fout.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "�����ϴ� �������� ������ �߻��Ͽ����ϴ�.", "���� ����", JOptionPane.OK_OPTION);
			}
		}
		
		public void LoadSetting() {	// ����� �ɼ��� �ҷ����� �Լ�
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
				
				defaultOptions[0] = options[0];	// �÷��̾� �̸�
				defaultOptions[1] = options[1];	// ���� ���丮
				defaultOptions[2] = options[2];	// ����� ���� ���丮
				defaultOptions[3] = options[3];	// ȿ���� ũ��
				defaultOptions[4] = options[4];	// ���� ����
				fin.close();
			}
			catch (IOException e) {
				
			}
		}
		public void ClearSaveFiles(boolean isCompleted) {
			File[] subFiles;
			if(isCompleted == true)
				subFiles = new File(defaultOptions[1]).listFiles();
			else
				subFiles = new File(defaultOptions[2]).listFiles();
			
			for(int i = 0; i < subFiles.length; i++) {
				File f = subFiles[i];
				String s = f.getName();
				int index = s.lastIndexOf(".txt");
				if(index != -1)
				{
					System.out.println(f.getPath() + " ����");
					f.delete();
				}
			}
		}
	}
}
