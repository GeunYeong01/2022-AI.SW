package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.Locale;

public class SubActivity extends AppCompatActivity {
    ImageButton mainbutton;
    TextView kindText;
    TextView tasteText;
    TextToSpeech tts;
    ImageButton text2;
    ImageButton text3;
    ImageButton kindbtn,tastebtn;

    public SoundPool soundPool;
    int soundID, soundID2,soundID3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);
        Intent intent = getIntent();
        String kind = intent.getStringExtra("음료의 종류");
        String taste = intent.getStringExtra("음료의 맛");

        kindText = findViewById(R.id.kindText);
        tasteText = findViewById(R.id.tasteText);
        text2 =findViewById(R.id.text2);
        text3 =findViewById(R.id.text3);
        kindbtn=findViewById(R.id.kind);
        tastebtn=findViewById(R.id.taste);

        kindText.setText(kind);
        tasteText.setText(taste);
        mainbutton=findViewById(R.id.mainbtn);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);    //작성
        soundID = soundPool.load(this, R.raw.kind, 1);
        soundID2 = soundPool.load(this, R.raw.taste, 1);
        soundID3 =soundPool.load(this, R.raw.soundmenu, 1);
        mainbutton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                mainbutton.setBackgroundResource(R.drawable.mainbtn);
                soundPool.play(soundID3,1f,1f,0,0,1f);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != ERROR){
                    tts.setPitch((float) 0.6);
                    tts.setSpeechRate((float) 0.8);
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        text2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                text2.setBackgroundResource(R.drawable.button_shape);
                tts.speak(kindText.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);

            }
        });
        text3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                text3.setBackgroundResource(R.drawable.button_shape);
                tts.speak(tasteText.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);

            }
        });
        kindbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kindbtn.setBackgroundResource(R.drawable.kind);
                soundPool.play(soundID,1f,1f,0,0,1f);
            }
        });
        tastebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tastebtn.setBackgroundResource(R.drawable.taste);
                soundPool.play(soundID2,1f,1f,0,0,1f);
            }
        });
    }
}