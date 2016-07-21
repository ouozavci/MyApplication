package com.example.oguzhan.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Shader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();

        Boolean isLogged = pref.getBoolean("isLogged", false);

        //Start login Activity first
        if (!isLogged) {

            Intent loginIntent = new Intent(this, loginActivity.class);
            startActivity(loginIntent);

        } else {

            String name = pref.getString("name", "error");
            String surname = pref.getString("surname", "error");
            String email = pref.getString("email", "error");
            textView = (TextView) findViewById(R.id.textView);
            textView.setText("name: " + name + "\n" + "surname: " + surname + "\nemail: " + email);

        }

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isLogged", false);
                editor.putString("name", "");
                editor.putString("surname","");
                editor.putString("email","");
                editor.commit();

                Intent loginIntent = new Intent(MainActivity.this, loginActivity.class);
                startActivity(loginIntent);
            }
        });

    }

}
