package scene;

import java.awt.*;
import javax.swing.*;

//��� �г�
public class BackgroundPanel extends JPanel {
	ImageIcon icon = new ImageIcon("images/quoridor.jpg");		// ���� ȭ�� ��� �̹���
	Image img = icon.getImage();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
	}
}