package scene;

import java.awt.*;
import javax.swing.*;

//배경 패널
public class BackgroundPanel extends JPanel {
	ImageIcon icon = new ImageIcon("images/quoridor.jpg");		// 시작 화면 배경 이미지
	Image img = icon.getImage();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
	}
}