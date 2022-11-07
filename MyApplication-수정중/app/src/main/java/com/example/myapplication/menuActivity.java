package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class menuActivity extends AppCompatActivity {
    ImageButton mainbutton2;
    ImageButton btnSttStart;
    EditText txtInMsg;
    EditText txtSystem;
    int myVoiceCode = 1234;
    public SoundPool soundPool;
    int soundID;

    Context cThis;
    String LogTT="[STT]";
    Intent SttIntent; //음성 인식용
    SpeechRecognizer mRecognizer;
    TextToSpeech tts; //음성 출력용

    protected void onCreate(Bundle saveInstanceState) {
        cThis=this;
        super.onCreate(saveInstanceState);
        setContentView(R.layout.menuactivity_main);
        btnSttStart = findViewById(R.id.menumic);
        txtInMsg=findViewById(R.id.tvVoice);
        txtSystem=findViewById(R.id.txtSystem);

        //음성인식

        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //음성인식 intent 생성
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());//데이터 설정
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//음성 인식 언어 설정
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);//음성인식 객체
        mRecognizer.setRecognitionListener(listener);// 음성인식 리스너 등록


       tts = new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onInit(int status) {
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }

            }
        });
        btnSttStart = findViewById(R.id.menumic);
        btnSttStart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                System.out.println("음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(menuActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
//                        mRecognizer.setRecognitionListener(listener);
                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

        //어플이 실행되면 자동으로 1초뒤에 음성 인식 시작
        /*new android.os.Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                txtSystem.setText("어플 실행됨--자동 실행-----------"+"\r\n"+txtSystem.getText());
                btnSttStart.performClick();
            }
        },1000);//바로 실행을 원하지 않으면 지워주시면 됩니다*/

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);    //작성
        soundID = soundPool.load(this, R.raw.soundmenu, 1);


        mainbutton2 = findViewById(R.id.mainbutton);
        mainbutton2.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                mainbutton2.setBackgroundResource(R.drawable.mainbutton);
                soundPool.play(soundID,1f,1f,0,0,1f);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
    public RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            txtSystem.setText("onReadyForSpeech..........."+"\r\n"+txtSystem.getText());
        }
        @Override
        public void onBeginningOfSpeech() {
            txtSystem.setText("지금부터 말을 해주세요..........."+"\r\n"+txtSystem.getText());
        }
        @Override
        public void onRmsChanged(float v) {

        }
        @Override
        public void onBufferReceived(byte[] bytes) {
            txtSystem.setText("onBufferReceived..........."+"\r\n"+txtSystem.getText());
        }
        @Override
        public void onEndOfSpeech() {
            txtSystem.setText("onEndOfSpeech..........."+"\r\n"+txtSystem.getText());
        }
        @Override
        public void onError(int i) {
            txtSystem.setText("천천히 다시 말해 주세요..........."+"\r\n"+txtSystem.getText());
        }
        @Override
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txtInMsg.setText(rs[0]+"\r\n"+txtInMsg.getText());
            FuncVoiceOrderCheck(rs[0]);
            mRecognizer.startListening(SttIntent);
            mRecognizer.destroy();
            }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }

    };

    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("안녕")>-1 || VoiceMsg.indexOf("안녕하세요")>-1){
            FuncVoiceOut("반가워");
        }
    }

    //음성 메세지 출력용
    private void FuncVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null);

        //어플이 종료할때는 완전히 제거

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    //카톡으로 이동을 했는데 음성인식 어플이 종료되지 않아 계속 실행되는 경우를 막기위해 어플 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }


}
