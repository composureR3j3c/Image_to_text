package com.example.bereket.imagetotext;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    SurfaceView cameravw;
    TextView textvw;
    CameraSource camerasrc;
    Button btnCall,btnSms,btnCpy,btnCont;
    final int RequestCameraPermissionId = 1001;
    String smsnum="";
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int grantResults[]) {
        switch (requestCode){
            case RequestCameraPermissionId:{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        camerasrc.start(cameravw.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameravw = (SurfaceView) findViewById(R.id.surface_View);
        textvw = (TextView) findViewById(R.id.text_View);
        btnCall = (Button) findViewById(R.id.buttonCall);
        btnSms = (Button) findViewById(R.id.buttonPhone);
        btnCont = (Button) findViewById(R.id.buttonMess);
        btnCpy = (Button) findViewById(R.id.buttoncpy);
        btnCall = (Button) findViewById(R.id.buttonCall);
        
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View View) {


                    String phone = textvw.getText().toString();

                    Intent ussdintent = new Intent(Intent.ACTION_CALL);
                    ussdintent.setData(Uri.parse("tel:"+ phone ));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(ussdintent);
            }

        });
        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View View) {

                smsnum = textvw.getText().toString();
            }
        });
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View View) {

                String mess=textvw.getText().toString();;
                SmsManager Sm= SmsManager.getDefault();
                Sm.sendTextMessage(smsnum,null,mess,null,null);
                callDialog();
            }

            private void callDialog() {
                    MessDialog messDialog=new MessDialog();
                messDialog.show(getSupportFragmentManager(), "example dialog");
            }
        });
        btnCpy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View View) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", textvw.getText().toString());
                clipboard.setPrimaryClip(clip);
            }
        });
       



        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {
            camerasrc = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameravw.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionId);
                            return;
                        }
                        camerasrc.start(cameravw.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    camerasrc.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items =detections.getDetectedItems();
                    if(items.size()!=0){
                        textvw.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i=0; i<items.size();++i){
                                    TextBlock item=items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textvw.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }
}
