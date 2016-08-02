package com.example.oguzhan.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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

import com.example.oguzhan.jsonlib.JSONParser;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.client.Firebase;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class loginActivity extends AppCompatActivity {

    LoginButton btnLoginFacebook;
    TextView tvResult;
    CallbackManager callbackManager;
    ProfileTracker tracker;
    AccessToken accessToken;
    AccessTokenTracker accessTokenTracker;

    Button btnSignUp;
    Button btnLogin;
    EditText txtEmail;
    EditText txtPass;


    private String getInfoUrl = Constants.SERVER_URL + "/test.php";
    private String url_add_user = Constants.SERVER_URL + "/create_product.php";

    //JSONParser jsonParser = new JSONParser();
    ProgressDialog pDialog;
    ShareDialog shareDialog;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);

        context = loginActivity.this;

        LoginManager.getInstance().logOut();

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                txtEmail = ((EditText) findViewById(R.id.txtEmail));
                txtPass = ((EditText) findViewById(R.id.txtPass));

                String email = txtEmail.getText().toString();
                String pass = txtPass.getText().toString();
                pass = encrypt(pass);

                try {


                    String result = login(email, pass);
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();

                } catch (Exception e) {

                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                }

            }
        });

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentSignup = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intentSignup);

            }
        });


        callbackManager = CallbackManager.Factory.create();
        tvResult = (TextView) findViewById(R.id.tvResult);

        btnLoginFacebook = (LoginButton) findViewById(R.id.login_button);
        btnLoginFacebook.setReadPermissions(Arrays.asList("public_profile", "email", "read_custom_friendlists,user_friends"));


        //Login with facebook part
        btnLoginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                Log.v("LoginActivity", response.toString());
                                String email = "";
                                String name = "";
                                String surname = "";
                                String id = "";

                                Log.v("FacebookJson", object.toString());

                                try {
                                    email = object.getString("email");
                                    name = object.getString("first_name");
                                    surname = object.getString("last_name");
                                    id = object.getString("id");
                                    String from = "fb";

                                    String[] strArray = {email, id};
                                    String result = login(email,id);//new getInfo().execute(strArray).get();

                                    if (result.equals("fail")) {
                                        String[] args = {name, surname, email, id, from};
                                        new signUser().execute(args);
                                    }

              /*                      SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                                    SharedPreferences.Editor editor = pref.edit();


                                    editor.putBoolean("isLogged", true);
                                    editor.putString("name", name);
                                    editor.putString("surname", surname);
                                    editor.putString("email", email);
                                    editor.commit();
*/
                                    Intent mainIntent = new Intent(getApplication(), MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        tracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tracker.stopTracking();
    }

    class getInfo extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(loginActivity.this);
            pDialog.setMessage("Connecting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            String email = args[0];
            String pass = args[1];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));

            JSONParser jsonParser = new JSONParser();

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(getInfoUrl,
                    "GET", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());


            // check for success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully created product

                    JSONArray jsonArray = json.getJSONArray("product");

                    JSONObject passDbjsn = (JSONObject) jsonArray.get(0);
                    String passDb = passDbjsn.getString("password");

                    if (passDb.equals(pass)) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putBoolean("isLogged", true);
                        editor.putString("name", passDbjsn.getString("name"));
                        editor.putString("surname", passDbjsn.getString("surname"));
                        editor.putString("email", passDbjsn.getString("email"));
                        editor.putString("fr", passDbjsn.getString("fr"));
                        editor.putString(Constants.UNIQUE_ID, passDbjsn.getString("firebaseid"));
                        editor.putString("phone", passDbjsn.getString("phone"));
                        editor.putString("id", passDb);
                        editor.commit();
                        if (passDbjsn.getString("phone").isEmpty()) {
                            Intent i = new Intent(getApplicationContext(), AskPhoneActivity.class);
                            startActivity(i);
                        } else {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                        pDialog.dismiss();
                        return "success";
                    } else {
                        pDialog.dismiss();
                        return "fail";
                    }
                    // closing this screen
                } else {
                    // failed to create product
                    return "fail";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "fail";
            }
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();

        }

    }


    class signUser extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(loginActivity.this);
            pDialog.setMessage("Connecting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            String name = args[0];
            String surname = args[1];
            String email = args[2];
            String password = args[3];
            String from = args[4];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("fr", from));

            Firebase firebase = new Firebase(Constants.FIREBASE_APP);
            Firebase newFirebase = firebase.push();

            Map<String, String> val = new HashMap<>();
            val.put("msg", "none");

            newFirebase.setValue(val);
            String uniqueId = newFirebase.getKey();

            params.add(new BasicNameValuePair("firebaseid", uniqueId));

            JSONParser jsonParser = new JSONParser();
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_add_user,
                    "POST", params);

            // check log cat fro response
            try {
                Log.d("Create Response", json.toString());
            } catch (NullPointerException e) {
                Log.e("JSon Null", "Json returned null from POST");
            }
            // check for success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully created product

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = pref.edit();

                    editor.putBoolean("isLogged", true);
                    editor.putString("name", name);
                    editor.putString("surname", surname);
                    editor.putString("email", email);
                    editor.putString("fr", from);
                    editor.putString(Constants.UNIQUE_ID, uniqueId);
                    editor.putBoolean(Constants.REGISTERED, true);
                    editor.commit();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }

    public String login(String email, String pass) {

        String[] strArray = {email, pass};
        String result = "fail";
        try {
            result = new getInfo().execute(strArray).get();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return result;
    }


    private String encrypt(String pass) {
        try {
            MessageDigest messageDigestNesnesi = MessageDigest.getInstance("MD5");
            messageDigestNesnesi.update(pass.getBytes());
            byte messageDigestDizisi[] = messageDigestNesnesi.digest();
            StringBuffer sb32 = new StringBuffer();
            for (int i = 0; i < messageDigestDizisi.length; i++) {
                sb32.append(Integer.toString((messageDigestDizisi[i] & 0xff) + 0x100, 32));
            }
            return sb32.toString();
        } catch (NoSuchAlgorithmException ex) {
            return "fail "/*+ex.getMessage()*/;
        }
    }
}
