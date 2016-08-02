package com.example.oguzhan.myapplication;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import java.io.FileReader;
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


    static String serverUrl = "http://192.168.137.1";//"http://loginappgplay.herokuapp.com";


    GameRequestDialog requestDialog;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // startService(new Intent(this, MessagingService.class));
      //  startService(new Intent(this, IdService.class));

        if(isRegistered())
        startService(new Intent(this,NotificationListener.class));

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
                Toast.makeText(getApplicationContext(), "Couldn't read the diary. Please check your connection!", Toast.LENGTH_LONG).show();
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
                                    // .setPromotionDetails("Example Promotion","EXAMPLE")
                            .build();
                    AppInviteDialog.show(MainActivity.this, content);
                }

            }
        });

        //Contacts butonu
        Button btnContacts = (Button) findViewById(R.id.btnContacts);
        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                        try {
                            ContactsFragment fr = new ContactsFragment();
                            fr.setArguments(getIntent().getExtras());
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.blank_fragment, fr).addToBackStack("fragment").commit();
                        } catch (Exception e) {
                            Snackbar.make(v, "Error!", Snackbar.LENGTH_SHORT).show();
                        }
                    else {
                        getPermissionToReadUserContacts();
                        getPermissionToCall();
                    }
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

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private static final int CALL_PHONE_PERMISSIONS_REQUEST = 2;


    public void getPermissionToReadUserContacts() {

        if (Build.VERSION.SDK_INT >= 23) {
            // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
            // checking the build version since Context.checkSelfPermission(...) is only available
            // in Marshmallow
            // 2) Always check for permission (even if permission has already been granted)
            // since the user can revoke permissions at any time through Settings
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // The permission is NOT already granted.
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_CONTACTS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST);
            }
        }
    }

    public void getPermissionToCall() {

        if (Build.VERSION.SDK_INT >= 23) {
            // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
            // checking the build version since Context.checkSelfPermission(...) is only available
            // in Marshmallow
            // 2) Always check for permission (even if permission has already been granted)
            // since the user can revoke permissions at any time through Settings
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                // The permission is NOT already granted.
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.CALL_PHONE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE_PERMISSIONS_REQUEST);
            }
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (Build.VERSION.SDK_INT >= 23) {
            // Make sure it's our original READ_CONTACTS request
            if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // showRationale = false if user clicks Never Ask Again, otherwise true
                    boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS);

                    if (showRationale) {
                        // do something here to handle degraded mode
                    } else {
                        Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == CALL_PHONE_PERMISSIONS_REQUEST) {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Call phone permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // showRationale = false if user clicks Never Ask Again, otherwise true
                    boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE);

                    if (showRationale) {
                        // do something here to handle degraded mode
                    } else {
                        Toast.makeText(this, "Call phone permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isRegistered() {
        //Getting shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);

        //Getting the value from shared preferences
        //The second parameter is the default value
        //if there is no value in sharedprference then it will return false
        //that means the device is not registered
        return sharedPreferences.getBoolean(Constants.REGISTERED, false);
    }
}
