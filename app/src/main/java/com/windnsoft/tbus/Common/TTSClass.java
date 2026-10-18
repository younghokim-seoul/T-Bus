package com.windnsoft.tbus.Common;

import android.speech.tts.TextToSpeech;

import com.windnsoft.tbus.General.WsLog;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class TTSClass implements TextToSpeech.OnInitListener {
    int status = TextToSpeech.STOPPED;

    TextToSpeech tts;
    static TTSClass instence = new TTSClass();

    public static TTSClass ttsInstecne() {
        return instence;
    }

    public TTSClass() {
        initialize(this);
    }

    private void initialize(TextToSpeech.OnInitListener listener) {
        this.tts = new TextToSpeech(BaseApplication.getApplication(), listener);

    }

    public void initialize() {
        return;
    }

    @Override
    public void onInit(int i) {

        status = i;
        WsLog.w("status" + i);
        if (status != TextToSpeech.ERROR) {
//            tts.setLanguage(Locale.KOREAN);
        }
    }


    public void stop() {
        tts.stop();
    }

    public void release() {
        tts.shutdown();
        tts = null;
    }


}
