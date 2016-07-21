package com.example.oguzhan.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Shader;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.internal.ShareFeedContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button btnLogout;
    ShareDialog shareDialog;

    String name = "";
    String surname = "";
    String email = "";

    String serverUrl = "http://192.168.137.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        shareDialog = new ShareDialog(MainActivity.this);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();

        Boolean isLogged = pref.getBoolean("isLogged", false);

        //Start login Activity first
        if (!isLogged) {

            Intent loginIntent = new Intent(this, loginActivity.class);
            startActivity(loginIntent);

        } else {

            name = pref.getString("name", "error");
            surname = pref.getString("surname", "error");
            email = pref.getString("email", "error");
            textView = (TextView) findViewById(R.id.textView);
            textView.setText("Merhaba " + name + " " + surname);

        }

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isLogged", false);
                editor.putString("name", "");
                editor.putString("surname", "");
                editor.putString("email", "");
                editor.commit();

                Intent loginIntent = new Intent(MainActivity.this, loginActivity.class);
                startActivity(loginIntent);
                finish();
            }

        });

        final EditText txtShare = (EditText) findViewById(R.id.txtShare);

        Button btnShare = (Button) findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToWall(txtShare.getText().toString());
            }
        });

    }

    private void shareToWall(String txt) {
        if (shareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle("My Diary")
                    .setContentDescription(txt)
                    .setContentUrl(Uri.parse(serverUrl + "/ShowText.php?email=" + email))
            .build();

            shareDialog.show(shareLinkContent);
        }
    }
}
