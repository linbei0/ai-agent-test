package com.ai.aiagent.tools;

import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Service
public class AudioInfoService {

    public AudioInfo parseAudioFile(String filePath) throws IOException, UnsupportedAudioFileException {
        File file = new File(filePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();

        AudioInfo info = new AudioInfo();
        info.setSampleRate(format.getSampleRate());
        info.setChannels(format.getChannels());
        info.setFormat(format.getEncoding().toString());

        return info;
    }
}

