package com.example.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity {
    TextView result;
    TextView confidence;
    TextView kindmain,tastemain;
    ImageView imageView;
    ImageButton picture;
    int imageSize=224;

    public SoundPool soundPool;
    int soundID, soundID2, soundID3;

    TextToSpeech tts;
    ImageButton text;
    ImageButton btn1;
    ImageButton classified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence=findViewById(R.id.confidence);
        imageView=findViewById(R.id.imageView);

        picture= findViewById(R.id.button);
        classified=findViewById(R.id.classified);
        tastemain=findViewById(R.id.tastemain);
        kindmain=findViewById(R.id.kindmain);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);    //작성
        soundID = soundPool.load(this, R.raw.soundbt, 1);
        soundID2 = soundPool.load(this, R.raw.candrink, 1);
        soundID3 = soundPool.load(this, R.raw.moredrink, 1);

        btn1=findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                btn1.setBackgroundResource(R.drawable.moredrink);
                soundPool.play(soundID3,1f,1f,0,0,1f);

                Intent intent = new Intent(getApplicationContext(), SubActivity.class);
                intent.putExtra("신뢰도",confidence.getText().toString());
                intent.putExtra("음료의 종류",kindmain.getText().toString());
                intent.putExtra("음료의 맛",tastemain.getText().toString());
                startActivity(intent);
            }
        });

        classified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classified.setBackgroundResource(R.drawable.candrink);
                soundPool.play(soundID2,1f,1f,0,0,1f);
            }
        });
        picture.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public  void onClick(View view){
                //Launch camera if we have permission
                picture.setBackgroundResource(R.drawable.take_picture);
                soundPool.play(soundID,1f,1f,0,0,1f);
                if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,1);
                }else{
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });

        text =findViewById(R.id.text);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        text.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                text.setBackgroundResource(R.drawable.button_shape);
                tts.speak(result.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);

            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize *3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel=0;
            for(int i =0; i<imageSize;i++){
                for(int j=0;j<imageSize;j++){
                    int val=intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val>>16)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val>>8)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val&0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i<confidences.length;i++){
                if(confidences[i]>maxConfidence){
                    maxConfidence=confidences[i];
                    maxPos=i;
                }
            }

            String[] classes={"코코팜","데미소다","칠성사이다","펩시","이프로","토레타","코카콜라","포도봉봉","몬스터옐로우","웰치스","트로피카나","몬스터그린"};
            String[] kind={"혼합음료","탄산음료","이온음료","과채음료"};
            String[] taste={"포도맛","복숭아맛","콜라맛","사이다맛","사과맛"};
            result.setText(classes[maxPos]);

            String s = "";
            s = s + String.format("%s: %.1f%%\n", classes[maxPos], confidences[maxPos] * 100);
            confidence.setText(s);
            switch(maxPos){
                case 0: case 11:
                    kindmain.setText(kind[0]);
                    tastemain.setText(taste[0]);
                    break;
                case 1:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[0]);
                    break;
                case 2:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[3]);
                    break;
                case 3:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[2]);
                    break;
                case 4:
                    kindmain.setText(kind[2]);
                    tastemain.setText(taste[1]);
                    break;
                case 5:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[1]);
                    break;
                case 6: case9:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[2]);
                    break;
                case 7:
                    kindmain.setText(kind[3]);
                    tastemain.setText(taste[0]);
                    break;
                case 8:
                    kindmain.setText(kind[2]);
                    tastemain.setText(taste[0]);
                    break;
                case 10:
                    kindmain.setText(kind[3]);
                    tastemain.setText(taste[1]);
                    break;
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public Bitmap rotateImage(Bitmap src, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),matrix,true);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1&&resultCode==RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension=Math.min(image.getWidth(),image.getHeight());
            image= ThumbnailUtils.extractThumbnail(image,dimension,dimension);
            image = rotateImage(image,0);
            imageView.setImageBitmap(image);

            image=Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
            classifyImage(image);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}