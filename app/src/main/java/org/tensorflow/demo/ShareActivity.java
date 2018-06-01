package org.tensorflow.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class ShareActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public String currentLang;
    private TextToSpeech tts;
    HashMap<String, String> myHashAudio = new HashMap<String, String>();

    int backBtnTap = 0;
    int headSetBtnTap = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMySelf();
            }
        });

        Intent intent = getIntent();
        currentLang = intent.getStringExtra("lang");
        //if (currentLang == null) currentLang = Locale.getDefault().getLanguage();

        String appPreference = "app_pref";
        SharedPreferences sharedpreferences;
        SharedPreferences.Editor editor;
        sharedpreferences = getSharedPreferences(appPreference, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        editor.putString("lang", currentLang);
        editor.apply();

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (!currentLang.equals("vi")) {
                    tts.setLanguage(Locale.US);
                    tts.speak("VIAS - Vehicles Detector has been ready", TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    tts.setLanguage(new Locale("vi"));
                    tts.setSpeechRate((float) 1.37);
                    myHashAudio.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                    tts.speak("Chế độ phát hiện phương tiện giao thông đã sẵn sàng", TextToSpeech.QUEUE_FLUSH, myHashAudio);
                }
            }
        });

        if (hasPermission()) {
            if (null == savedInstanceState) {
                setFragment();
            }
        } else {
            requestPermission();
        }
    }

    private void stopMySelf(){
        showToastMessage("Đã thoát chế độ phát hiện phương tiện giao thông, trở về màn hình chính", "Close VIAS - Vehicles Detector");
        Speak("Đã thoát chế độ phát hiện phương tiện giao thông, trở về màn hình chính", "close VIAS - Vehicles Detector");
        finishAndRemoveTask();
    }


    private void showToastMessage(final String viText, final String enText){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (currentLang){
                    case "vi":
                        Toast.makeText(getApplicationContext(), viText, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), enText, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void Speak(String viText, String enText){

        switch (currentLang){
            case "vi":
                tts.speak(viText, TextToSpeech.QUEUE_FLUSH, myHashAudio);
                break;
            default:
                tts.speak(enText, TextToSpeech.QUEUE_FLUSH, myHashAudio);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backBtnTap++;
            if (backBtnTap >= 2) {
                showToastMessage("Đã thoát chế độ phát hiện giao thông", "Close VIAS - Vehicles Detector");
                Speak("Đã thoát chế độ phát hiện phương tiện giao thông", "close VIAS - Vehicles Detector");

                finishAndRemoveTask();
                return super.onKeyDown(keyCode, event);
            } else {
                showToastMessage("Chạm lần nữa để thoát chế độ phát hiện phương tiện giao thông", "Tap again to exit VIAS - Vehicles Detector");
                Speak("Chạm lần nữa để thoát chế độ phát hiện phương tiện giao thông", "Tap again to exit VIAS - Vehicles Detector");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backBtnTap = 0;
                    }
                }, 3500);
            }
        }
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
            headSetBtnTap++;
            if (headSetBtnTap >= 2) {
                showToastMessage("Đã thoát chế độ phát hiện giao thông", "Close VIAS - Vehicles Detector");
                Speak("Đã thoát chế độ phát hiện phương tiện giao thông", "close VIAS - Vehicles Detector");

                finishAndRemoveTask();
                return super.onKeyDown(keyCode, event);
            } else {
                showToastMessage("Ấn lần nữa để thoát chế độ phát hiện phương tiện giao thông", "Tap again to exit VIAS - Vehicles Detector");
                Speak("Ấn lần nữa để thoát chế độ phát hiện phương tiện giao thông", "Tap again to exit VIAS - Vehicles Detector");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        headSetBtnTap = 0;
                    }
                }, 3000);
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(ShareActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    private void setFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, CameraConnectionFragment.newInstance())
                .commit();
    }
}
