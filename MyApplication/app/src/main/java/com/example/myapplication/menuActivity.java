package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class menuActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    ImageButton mainbutton2;
    EditText txtSystem;

    public SoundPool soundPool;
    int soundID;

    Intent intent;
    TextToSpeech tts;
    ImageButton sttBtn;
    EditText textView;
    EditText getTxtSystem;
    final int PERMISSION = 1;

    private EditText txtText;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.menuactivity_main);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);    //작성
        soundID = soundPool.load(this, R.raw.soundmenu, 1);

        // 퍼미션 체크
        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        textView = findViewById(R.id.txtInMsg);
        getTxtSystem = findViewById(R.id.txtSystem);
        sttBtn = findViewById(R.id.menumic);

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");   // 텍스트로 변환시킬 언어 설정

        sttBtn.setOnClickListener(v -> {
            SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);

            tts = new TextToSpeech(menuActivity.this, (TextToSpeech.OnInitListener) this);
        });

        mainbutton2 = findViewById(R.id.mainbutton);
        mainbutton2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mainbutton2.setBackgroundResource(R.drawable.mainbutton);
                soundPool.play(soundID,1f,1f,0,0,1f);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            String startvoice="음성인식을 시작합니다.";
            //funcVoiceOut(startvoice);
            Toast.makeText(getApplicationContext(),startvoice,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
            funcVoiceOut(message);
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어준다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String resultStr="";

            for(int i= 0; i < matches.size() ; i++) {
                textView.setText(matches.get(i));
                resultStr+=matches.get(i);
            }
            speakOut(resultStr);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut(String resultStr) {
        if(resultStr.indexOf("캔 구별")>-1){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if(resultStr.indexOf("포도")>-1){
            String drink="포도맛 음료는 웰치스, 포도 봉봉, 코코팜 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        if(resultStr.indexOf("복숭아")>-1){
            String drink="복숭아맛 음료는 이프로, 트로피카나 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        if(resultStr.indexOf("사과")>-1){
            String drink="사과맛 음료는 데미소다 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        if(resultStr.indexOf("레몬")>-1){
            String drink="레몬맛 음료는 몬스터엘로우 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        if(resultStr.indexOf("이온")>-1){
            String drink="이온음료는 이프로, 토레타, 포카리스웨트 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        if(resultStr.indexOf("탄산")>-1){
            String drink="탄산음료는 펩시, 코카콜라, 칠성사이다, 웰치스, 트로피카나 입니다.";
            getTxtSystem.setText(drink);
            tts.setPitch((float) 0.6);
            tts.setSpeechRate((float) 0.8);
            tts.speak(getTxtSystem.getText(), TextToSpeech.QUEUE_FLUSH, null, "id1");
        }
        else{
            //funcVoiceOut("틀렸음");
        }
    }
    public void funcVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;
        if(!tts.isSpeaking()) {
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    @Override
    public void onDestroy() {
        if (tts != null)  {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setPitch(1);
        } else {
            Log.e("TTS", "초기화 실패");
        }
    }
}
