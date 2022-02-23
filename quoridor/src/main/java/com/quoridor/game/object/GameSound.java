package com.quoridor.game.object;

import java.io.*;
import javax.sound.sampled.*;

import com.quoridor.game.manager.OptionManager;

public class GameSound {
    private File file;
    private float volume;
    private float maxVolume;

    GameSound(String root, int volume) { 
        file = new File(root);
        maxVolume = OptionManager.getInstance().MAX_VOLUME;
        this.volume = (volume / 2 + 10) / maxVolume;
    }

    public void play() {
        Thread soundThread = new Thread(new SoundThread());
        soundThread.start();
    }
    
    class SoundThread implements Runnable {
        public void run() {
            try {
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(inputStream);
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = control.getMaximum() - control.getMinimum();	// 범위 계산
                float result = (range * volume) + control.getMinimum();		// 최종 볼륨값
                control.setValue(result);									// 볼륨 설정
                clip.start();												// 소리 재생
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }	
}
