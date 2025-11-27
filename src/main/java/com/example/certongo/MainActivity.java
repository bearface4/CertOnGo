package com.example.certongo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

        private static final long SPLASH_DELAY = 1000;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            FirebaseApp.initializeApp(this);

            new Handler().postDelayed(() -> {
                Intent mainIntent = new Intent(MainActivity.this, SignIn.class);
                startActivity(mainIntent);
                finish();
            }, SPLASH_DELAY);
        }
    }