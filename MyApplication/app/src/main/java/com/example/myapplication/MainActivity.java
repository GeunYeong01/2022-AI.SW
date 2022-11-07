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
    TextView result; //인식된 캔음료 이름
    TextView confidence; //모델 신뢰도
    TextView kindmain,tastemain; //음료의 종류, 음료의 맛 (SubActivity로 넘겨주기 위함)
    ImageView imageView; // 캔음료 이미지
    ImageButton picture; // 사진찍기 버튼
    int imageSize=224; //이미지 사이즈

    public SoundPool soundPool; // 사운드 생성자 
    int soundID, soundID2, soundID3; // 사운드변수지정

    TextToSpeech tts; // tts 생성자
    ImageButton text; //캔음료 읽어주는 마이크버튼
    ImageButton btn1; //SubActivity로 넘어가는 버튼
    ImageButton classified; // "캔 음료" 읽어주는 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result); // 레이아웃과 연결
        confidence=findViewById(R.id.confidence);
        imageView=findViewById(R.id.imageView);
        picture= findViewById(R.id.button);
        classified=findViewById(R.id.classified);
        tastemain=findViewById(R.id.tastemain);
        kindmain=findViewById(R.id.kindmain);
        btn1=findViewById(R.id.btn1);
        text =findViewById(R.id.text);

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0); // 사운드 객체 생성
        soundID = soundPool.load(this, R.raw.soundbt, 1); //Take Picture
        soundID2 = soundPool.load(this, R.raw.candrink, 1); // 캔 음료
        soundID3 = soundPool.load(this, R.raw.moredrink, 1); // 음료 더 알아보기

        picture.setOnClickListener(new View.OnClickListener(){ // 사진버튼 리스너 달아주기
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public  void onClick(View view){ //클릭했을때
                //카메라 권한 부여
                if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,1);
                }else{
                    //카메라 권한 부여 안 함
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
                //사운드 설정
                picture.setBackgroundResource(R.drawable.take_picture);
                soundPool.play(soundID,1f,1f,0,0,1f);
            }
        });

        classified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사운드 설정
                classified.setBackgroundResource(R.drawable.candrink);
                soundPool.play(soundID2,1f,1f,0,0,1f);
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                //사운드 설정
                btn1.setBackgroundResource(R.drawable.moredrink);
                soundPool.play(soundID3,1f,1f,0,0,1f);
                //SubActivity로 데이터 넘겨주기
                Intent intent = new Intent(getApplicationContext(), SubActivity.class);
                intent.putExtra("음료의 종류",kindmain.getText().toString());
                intent.putExtra("음료의 맛",tastemain.getText().toString());
                startActivity(intent); // SubActivity 실행
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() { //TTS 객체 생성
            @Override
            public void onInit(int i) {
                if(i != ERROR){
                    tts.setPitch((float) 0.6); //음성 톤 0.6배 내려주기
                    tts.setSpeechRate((float) 0.8); // 읽는 속도 0.8배 빠르기로 설정
                    tts.setLanguage(Locale.KOREAN); //언어선택
                }
            }
        });

        text.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                text.setBackgroundResource(R.drawable.button_shape); //버튼 모양 설정
                tts.speak(result.getText().toString(),TextToSpeech.QUEUE_FLUSH,null); //result 읽어주기

            }
        });
    }

    public void classifyImage(Bitmap image){ //이미지 구별하기
        try {
            // 모델을 생성하고 애플리케이션 컨텍스트 가져옴
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());
            //모델에 대한 입력
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            //모델에 전달 할 수 있는 바이트 버퍼 생성
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize *3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int [] intValues = new int[imageSize*imageSize]; //비트맵 이미지에서 픽셀 값 가져오기
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel=0;
            for(int i =0; i<imageSize;i++){ //배열을 통해 반복하고 해당 픽셀 값을 바이트 버퍼에 추가하여 변수를 갖도록 함
                for(int j=0;j<imageSize;j++){
                    int val=intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val>>16)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val>>8)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val&0xFF)*(1.f/255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);
            // 모델 결과값 (모델 추론 실행)
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();//신뢰도 결과값 가져오기
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i<confidences.length;i++){
                if(confidences[i]>maxConfidence){
                    maxConfidence=confidences[i];
                    maxPos=i; // 신뢰도가 가장 큰 인덱스 받기
                }
            }

            String[] classes={"코코팜","데미소다","칠성사이다","펩시","이프로","토레타","코카콜라","포도봉봉","몬스터옐로우","웰치스","트로피카나","몬스터그린"};
            String[] kind={"혼합음료","탄산음료","이온음료","과채음료"};
            String[] taste={"포도맛","복숭아맛","콜라맛","사이다맛","사과맛","레몬맛"};
            result.setText(classes[maxPos]);

            String s = "";
            s = s + String.format("%s: %.1f%%\n", classes[maxPos], confidences[maxPos] * 100);
            confidence.setText(s);
            switch(maxPos){
                case 0:
                    kindmain.setText(kind[0]);
                    tastemain.setText(taste[0]);
                    break;
                case 1: case 5: case 11:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[4]);
                    break;
                case 2:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[3]);
                    break;
                case 3: case 6:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[2]);
                    break;
                case 4:
                    kindmain.setText(kind[2]);
                    tastemain.setText(taste[1]);
                    break;
                case 7:
                    kindmain.setText(kind[3]);
                    tastemain.setText(taste[0]);
                    break;
                case 8:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[5]);
                    break;
                case 9:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[0]);
                    break;
                case 10:
                    kindmain.setText(kind[1]);
                    tastemain.setText(taste[1]);
                    break;
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public Bitmap rotateImage(Bitmap src, float degree){ // 이미지가 회전되어 나올때 돌리는 함수
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),matrix,true);
    }

    @Override //카메라 시작 후 인텐트를 사용하여 이미지라는 비트맵 가져오기
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1&&resultCode==RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data"); // 비트맵 데이터 설정
            int dimension=Math.min(image.getWidth(),image.getHeight()); // 신경망에 정사각형 이미지 공급
            image= ThumbnailUtils.extractThumbnail(image,dimension,dimension); // 축소판 추출(일부 축소판 유틸리티 사용)
            image = rotateImage(image,-90); // 이미지가 회전될 때 사용
            imageView.setImageBitmap(image);// imageView에 가져온 image 넣기

            image=Bitmap.createScaledBitmap(image,imageSize,imageSize,false);
            classifyImage(image); //이미지 비트맵 전달
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}