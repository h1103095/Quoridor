package com.quoridor.utils;

import java.util.Calendar;

import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.game.object.GameAction;
import com.quoridor.logger.MyLogger;

import java.io.*;
import java.text.SimpleDateFormat;

public final class Utils {
	public static String onePlayerButtonImage = "images/Single.png";

	public static String configFilePath = "config.properties";

    public static String getDate()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HHmmss");
		Calendar time = Calendar.getInstance();
		String dateString = dateFormat.format(time.getTime());
		
		return dateString;
	}

	// 폴더 생성
	public static void makeDir(String dirName)
	{
		File Folder = new File(dirName);
		if(!Folder.exists()) {
			Folder.mkdir();
		}
	}

	public static void logAction(PLAYER_COLOR playerColor, GameAction action) {
		String stringToPrint = playerColor.name() + " ";
		if(action.getActionMode().isWallMode()) {
			stringToPrint += "Wall ";
		} else {
			stringToPrint += "Move ";
		}
		stringToPrint += Integer.toString(action.getPoint().y) + ", " + Integer.toString(action.getPoint().x);
		MyLogger.getInstance().info(stringToPrint);
	}
}
