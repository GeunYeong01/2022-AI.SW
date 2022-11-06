package com.example.myapplication;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class menuActivity extends AppCompatActivity {
    ImageButton mainbutton2;

    public SoundPool soundPool;
    int soundID;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.menuactivity_main);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);    //작성
        soundID = soundPool.load(this, R.raw.soundmenu, 1);


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
}
