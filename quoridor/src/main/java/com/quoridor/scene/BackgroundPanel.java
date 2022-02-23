package com.quoridor.scene;

import java.awt.*;
import javax.swing.*;

//배경 패널
public class BackgroundPanel extends JPanel {
	// 시작 화면 배경 이미지
	ImageIcon icon = new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\quoridor.jpg");
	Image img = icon.getImage();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
	}
}