/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*  This file has been modified by Nataniel Ruiz affiliated with Wall Lab
 *  at the Georgia Institute of Technology School of Interactive Computing
 */

package org.tensorflow.demo;

import android.Manifest;
import android.content.Context;
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

public class CameraActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public String currentLang;
    private TextToSpeech tts;

    int backBtnTap = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMyself();
            }
        });

        currentLang = Locale.getDefault().getLanguage();

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
                    tts.setSpeechRate((float) 1.30);
                    HashMap<String, String> myHashAudio = new HashMap<String, String>();
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

    private void stopMyself(){
        showToastMessage("Đã thoát chế độ phát hiện phương tiện giao thông, trở lại màn hình chính", "Close VIAS - Vehicles Detector, return to main screen");
        Speak("Đã thoát chế độ phát hiện phương tiện giao thông, trở lại màn hình chính", "close VIAS - Vehicles Detector, return to main screen");
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
        HashMap<String, String> myHashAudio = new HashMap<String, String>();
        myHashAudio.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
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
                showToastMessage("Đã thoát chế độ phát hiện phương tiện giao thông", "Close VIAS - Vehicles Detector");
                Speak("Đã thoát chế độ phát hiện phương tiện giao thông", "close VIAS - Vehicles Detector");

                finish();
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

  /*@Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1234) {
      // Truong hop co gia tri tra ve
      if (resultCode == RESULT_OK) {
        ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (!textMatchList.isEmpty()) {
          String request = textMatchList.get(0).toString();
          request = request.toLowerCase();
          if (request.contains("thoát") || request.contains("tắt") || request.contains("đóng")
                  || request.contains("exit") || request.contains("quit") || request.contains("close")){
            stopMyself();
          } else if (request.contains("dẫn đường đến ") || request.contains("navigate to ")) {
            String destination;
            if (currentLang.equals("vi")) {
              destination = request.replace("dẫn đường đến ", "");
              tts.speak("bắt đầu dẫn đường đến " + destination +
                              ", để biết hướng đi, hãy chạm vào góc trái phía trên màn hình",
                      TextToSpeech.QUEUE_FLUSH, null);
            } else {
              destination = request.replace("navigate to ", "");
              tts.speak("Start navigating to " + destination +
                              ", to get direction, just tap on the upper-left corner of the screen",
                      TextToSpeech.QUEUE_FLUSH, null);
            }
            startService(new Intent(CameraActivity.this, NavigationService.class));

            Uri gmmIntentUri = Uri.parse("google.navigation:q="+destination+"&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

            finishAndRemoveTask();
          }
        }
      }
    }
  }*/

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
                Toast.makeText(CameraActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
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