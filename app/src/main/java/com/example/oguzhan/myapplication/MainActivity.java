package com.example.oguzhan.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oguzhan.jsonlib.JSONParser;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.internal.ShareFeedContent;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.GameRequestDialog;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button btnLogout;
    ShareDialog shareDialog;
    ProgressDialog pDialog;

    String name = "";
    String surname = "";
    String email = "";
    String fr = "";
    String userId = "";


    static String serverUrl = "http://192.168.1.32";

    GameRequestDialog requestDialog;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        shareDialog = new ShareDialog(MainActivity.this);
        setContentView(R.layout.activity_main);



        final EditText txtDiary = (EditText) findViewById(R.id.txtShare);

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
            fr = pref.getString("fr", "error");
            userId = pref.getString("id", "error");
            textView = (TextView) findViewById(R.id.textView);
            textView.setText("Merhaba " + name + " " + surname);


            String diary = "";
            try {
                diary = new readDiary().execute().get();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Couldn't read the diary", Toast.LENGTH_LONG).show();
            }
            if (!diary.equals("")) {
                txtDiary.setText(diary);
            }

        }


        //Kaydet butonu
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String diary = txtDiary.getText().toString();
                    String[] params = {diary};
                    String result = new saveDiary().execute(params).get();
                    if (result.equals("success")) {
                        Toast.makeText(getApplicationContext(), "Saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Oops!Something happened...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {

                }

            }
        });

        //Logout butonu
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isLogged", false);
                editor.putString("name", "");
                editor.putString("surname", "");
                editor.putString("email", "");
                editor.putString("fr", "");
                editor.commit();

                Intent loginIntent = new Intent(MainActivity.this, loginActivity.class);
                startActivity(loginIntent);
                finish();
            }

        });

        //Paylaş butonu
        Button btnShare = (Button) findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToWall();
            }
        });
        
        //Recommend butonu
        final String appLinkUrl = "https://fb.me/154257521647711";//developers.facebook.com dan alınan applink
        final String previewImageUrl = "https://pixabay.com/static/uploads/photo/2015/10/01/21/39/background-image-967820_960_720.jpg";//Uygulama resim urlsi
        Button btnRecommend = (Button) findViewById(R.id.btnRecommend);
        btnRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(previewImageUrl)
                            .build();
                    AppInviteDialog.show(MainActivity.this, content);
                }

            }
        });
    }

    //Paylaş methodu
    private void shareToWall() {
        if (shareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle("My Diary")
                    .setContentDescription("Diary of " + name)
                    .setContentUrl(Uri.parse(serverUrl + "/ShowText.php?email=" + email))
                    .build();

            shareDialog.show(shareLinkContent);
        }
    }



    class saveDiary extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            JSONParser jsonParser = new JSONParser();

            String diary = params[0];

            List<NameValuePair> args = new ArrayList<NameValuePair>();
            args.add(new BasicNameValuePair("email", email));
            args.add(new BasicNameValuePair("Diary", diary));
            JSONObject jsonObject = jsonParser.makeHttpRequest(serverUrl + "/setDiary.php", "POST", args);

            Log.d("Create Response", jsonObject.toString());

            String result = "";

            try {
                int success = jsonObject.getInt("success");
                if (success == 1) return "success";
                else return "fail";
            } catch (Exception e) {
                Log.e("JSonException", e.getMessage());
                return "fail";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Connecting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
        }
    }

    class readDiary extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> args = new ArrayList<NameValuePair>();
            args.add(new BasicNameValuePair("email", email));
            JSONObject jsonObject = jsonParser.makeHttpRequest(serverUrl + "/getDiary.php", "GET", args);

            Log.d("Create Response", jsonObject.toString());
            String diary = "";
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("product");

                JSONObject diaDb = (JSONObject) jsonArray.get(0);
                diary = diaDb.getString("Diary");
            } catch (Exception e) {
                Log.e("JSonException", e.getMessage());
            }
            return diary;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Connecting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
        }
    }
}
