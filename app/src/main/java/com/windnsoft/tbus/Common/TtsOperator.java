package com.windnsoft.tbus.Common;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * TTS (TextToSpeech) 을 이용하여 말하기 구성
 * 1. Context , TTSSpeakerListener 는 입력한다.
 * 2. Start() 로 실행한다. ( TTS 초기화 됨.)
 * 3. Speech로 문자를 전달.
 * <p/>
 * TTSSpeakerListener 로 상태에 대해서 수신 받을 수 있다.
 *
 * @author PPSOne
 */
public class TtsOperator {
    public interface TtsOperatorListener {
        void InitializationCompleted();

        void InitializationError(int status);

        void LanguageError(int status);

        void UtteranceProgressListenerError();

        void UtteranceStart();

        void UtteranceDone();

        void UtteranceError();
    }

    private static final TtsOperator uniqueInstance = new TtsOperator();
    private final static String TAG = TtsOperator.class.getSimpleName();
    Context context;
    MyApplication app;
    HashMap<String, String> map;
    TtsOperatorListener listener = null;
    private static TextToSpeech mTTS = null;
    private boolean bTTSInit, bTTSSupport, bUse = false, bForce = false, bByForce = false;
    int oldVolume;

    private TtsOperator() {
        map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID20150715");
    }

    public static TtsOperator getInstance() {
        return uniqueInstance;
    }


    /**
     * TTS 초기화, 언어설정, 말하기 진행에 대한 리스너 설정.
     */
    private void TTSInit() {
        if (context == null) {
            return;
        }

        if (mTTS != null) {
            mTTS.stop();
        }

        mTTS = new TextToSpeech(context, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    bTTSInit = true;

                    int available = mTTS.isLanguageAvailable(Locale.KOREA);    //해당 언어가 지원하는 언어인지 검사

                    if (available < 0) {                    //지원하지 않으면 메시지 출력
                        bTTSSupport = false;
                        listener.LanguageError(available);
                    } else {
                        bTTSSupport = true;
                        Locale lang = Locale.KOREA;
                        mTTS.setLanguage(lang);        //언어 설정.
                        mTTS.setPitch(1.5f);                //pitch 설정.  0.5~2.0
                        mTTS.setSpeechRate(1.5f);        //rate 설정.  0.5~2.0
                        int success = mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                listener.UtteranceStart();
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                listener.UtteranceDone();
                                bForce = false;
                            }

                            @Override
                            public void onError(String utteranceId) {
                                listener.UtteranceError();
                                bForce = false;
                            }
                        });

                        if (success == TextToSpeech.SUCCESS) {
                            listener.InitializationCompleted();
                        } else {
                            listener.UtteranceProgressListenerError();
                        }
                    }
                } else {
                    bTTSInit = false;
                    listener.InitializationError(status);
                }
            }
        });
    }

    /**
     * context 등록. startPlay 하기전에 등록 필요.
     *
     * @param context
     */
    public void Context(Context context) {
        this.context = context.getApplicationContext();
        app = (MyApplication) context.getApplicationContext();
    }

    /**
     * 초기화, 재생, 중지, 오류등에 대한 리스너 등록.
     * startPlay 하기전에 등록 필요.
     *
     * @param listener
     */
    public void TTSSpeakerListener(TtsOperatorListener listener) {
        this.listener = listener;
    }

    /**
     * 입력 받은 문자를 TTS 로 실행이 됨.
     *
     * @param str
     */
    public void Speech(String str) {
        if (bUse == false) {  // 사용 안하고 있을때 우선 확인.
            bByForce = false;
            return;
        } else if (bForce) { // 강제 출력이 우선
            bByForce = false;
            return;
        } else if (!bTTSInit) {                //초기화 안되었으면 메시지 출력
            bByForce = false;
            return;
        } else if (!bTTSSupport) {    //지원하지 않는 언어이면 메시지 출력
            bByForce = false;
            return;
        } else {
            if (bByForce) {
                bForce = true;
                bByForce = false;
            }
            mTTS.speak(str, TextToSpeech.QUEUE_FLUSH, map);    //해당 언어로 텍스트 음성 출력 , map 등록(ID 설정)으로 말하기 상태를 확인함.
        }
    }

    public void SpeechByForce(final String str) {
        bByForce = true;
        if (bUse) {  // 설정중일 때
            Speech(str);
        } else {
            Use(true);
            TTSSpeakerListener(new TtsOperatorListener() {
                @Override
                public void InitializationCompleted() {
                    Speech(str);
                }

                @Override
                public void InitializationError(int status) {
                }

                @Override
                public void LanguageError(int status) {
                }

                @Override
                public void UtteranceProgressListenerError() {
                }

                @Override
                public void UtteranceStart() {
                }

                @Override
                public void UtteranceDone() {
                    Clear();
                    Use(false);
                }

                @Override
                public void UtteranceError() {
                    Clear();
                    Use(false);
                }
            });
            Start();
        }
    }

    public void Start() {
        if (context == null) {
            return;
        }
        if (listener == null) {
            return;
        }

        TTSInit();
    }

    /**
     * 말하기 중지
     */
    public void Stop() {
        if (mTTS != null) {
            mTTS.stop();
        }
    }

    public void Clear() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
            bTTSInit = false;
            bTTSSupport = false;
        }
    }

    public boolean isTTSInit() {
        return bTTSInit;
    }

    public void Use(boolean bUse) {
        this.bUse = bUse;
    }
}
