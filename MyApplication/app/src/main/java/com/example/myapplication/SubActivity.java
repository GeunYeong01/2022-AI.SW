package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import android.annotation.TargetApi;
import android.content.Intent;
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
    Button mainbutton;
    TextView kindText;
    TextView tasteText;
    TextToSpeech tts;
    ImageButton text2;
    ImageButton text3;
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

        kindText.setText(kind);
        tasteText.setText(taste);
        mainbutton=findViewById(R.id.mainbtn);
        mainbutton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != ERROR){
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
    }
}