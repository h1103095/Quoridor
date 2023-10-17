package com.quoridor.game.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import com.quoridor.utils.Utils;
import com.quoridor.enums.GAME_OPTION;
import com.quoridor.logger.MyLogger;


public final class OptionManager {
    public static Properties properties;
    public static String defaultPlayerName = "Player";
    public static String defaultCompletedGameSaveDirectory = "./completed_save";
    public static String defaultIncompletedGameSaveDirectory = "./incompleted_save";
    public static String defaultVolume = "10";
    public static String defaultNumWalls = "10";

    private static OptionManager instance = new OptionManager();

    public int MAX_VOLUME = 20;
    public int MAX_NUM_WALLS = 20;

    private static String emptyProperty = "NULL";

    private OptionManager() {
        properties = new Properties();
		// 옵션 불러오기
		loadConfig(Utils.configFilePath);
    }

    public static OptionManager getInstance() {
        return instance;
    }

    public int getNumConfig() {
        return GAME_OPTION.values().length;
    }

    public String getConfig(GAME_OPTION key) {
        String stringKey = key.toString();
        if(properties.containsKey(stringKey)) {
            return properties.getProperty(stringKey);
        } else {
            return emptyProperty;
        }
    }

    public void setConfig(GAME_OPTION optionEnum, String val) {
        properties.setProperty(optionEnum.toString(), val);
    }

    public void initialize() {
        setConfig(GAME_OPTION.PLAYER_NAME, defaultPlayerName);
        setConfig(GAME_OPTION.COMPLETED_GAME_SAVE_DIRECTORY, defaultCompletedGameSaveDirectory);
        setConfig(GAME_OPTION.INCOMPLETED_GAME_SAVE_DIRECTORY, defaultIncompletedGameSaveDirectory);
        setConfig(GAME_OPTION.VOLUME, defaultVolume);
        setConfig(GAME_OPTION.NUM_WALLS, defaultNumWalls);
    }

    public boolean saveConfig(String configFilePath) {
        try {
            OutputStream outputStream = new FileOutputStream(configFilePath);
            properties.store(outputStream, null);
            outputStream.close();
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadConfig(String configFilePath) {
        FileInputStream fileInputStream;
        File propertiesFile = new File(Utils.configFilePath);
        if(!propertiesFile.exists()) {
            initialize();
            if(saveConfig(configFilePath)) {
                return true;
            } else {
                return false;
            }
        } else {
            try {
                fileInputStream = new FileInputStream(configFilePath);
                properties.load(new BufferedInputStream(fileInputStream));
                fileInputStream.close();
                return true;
            } catch (IOException e) {
                MyLogger.getInstance().warning("Can't open config file!");
                return false;
            }
        }
    }
}