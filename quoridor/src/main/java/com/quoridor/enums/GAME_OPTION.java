package com.quoridor.enums;

public enum GAME_OPTION {
    PLAYER_NAME("이름 변경", false),
    COMPLETED_GAME_SAVE_DIRECTORY("저장 폴더 변경", false),
    INCOMPLETED_GAME_SAVE_DIRECTORY("복기용 게임 폴더 변경", false),
    VOLUME("효과음 크기", true),
    NUM_WALLS("장애물 갯수", true);

    private final String optionLabel;
    private final boolean numericString;
    
    private GAME_OPTION(String optionLabel, boolean numericString) {
        this.optionLabel = optionLabel;
        this.numericString = numericString;
    }

    public String getOptionLabel() { return optionLabel; }
    public boolean isNumeric() { return numericString; }
}
