package com.example.oguzhan.myapplication;
import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oguzhan.jsonlib.JSONParser;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.GameRequestDialog;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DiaryFragment extends Fragment {

    TextView textView;
    Button btnLogout;
    ShareDialog shareDialog;
    ProgressDialog pDialog;

    String name = "";
    String surname = "";
    String email = "";
    String fr = "";
    String userId = "";
    String firebaseid = "";


    GameRequestDialog requestDialog;
    CallbackManager callbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary, container, false);
        
        /*Initializin facebook sdk. Facebook sdk is used for recommend and share
        methods here *****/
        FacebookSdk.sdkInitialize(getContext());

        /*MyPref is used to get logged in user information
            it contains isLogged?-name-surname-email-password-from(app or facebook account)-firebaseid(id for notification service),
                if user logged in with a facebook account his facebook id is stored in password row
         */
        SharedPreferences pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();

        //Share window initalized
        shareDialog = new ShareDialog(getActivity());

        /*Initializing view components here */
        final EditText txtDiary = (EditText) rootView.findViewById(R.id.txtShare);
        Button btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnLogout = (Button) rootView.findViewById(R.id.btnLogout);
        Button btnShare = (Button) rootView.findViewById(R.id.btnShare);
        Button btnRecommend = (Button) rootView.findViewById(R.id.btnRecommend);
        Button btnContacts = (Button) rootView.findViewById(R.id.btnContacts);

        /*Check if user logged in from isLogged preference. */
        Boolean isLogged = pref.getBoolean("isLogged", false);
        /*Start login Activity first if user is NOT logged.*/
        if (!isLogged) {
            /**************************************************************************************/
            Intent loginIntent = new Intent(getActivity(), loginActivity.class);
            startActivity(loginIntent);
            getActivity().finish();
            /**************************************************************************************/
        } else {
        /* after logged in start notification listener service*/


        /*get other information samples from preferences*/
            name = pref.getString("name", "error");
            surname = pref.getString("surname", "error");
            email = pref.getString("email", "error");
            fr = pref.getString("fr", "error");
            userId = pref.getString("id", "error");
            firebaseid = pref.getString("firebaseid", "error");


            textView = (TextView) rootView.findViewById(R.id.textView);
            textView.setText("Merhaba " + name + " " + surname);


            String diary = "";
            try {
                diary = new readDiary().execute().get();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Couldn't read the diary. Please check your connection!", Toast.LENGTH_LONG).show();
            }
            if (!diary.equals("")) {
                txtDiary.setText(diary);
            }

        }


        //Kaydet butonu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String diary = txtDiary.getText().toString();
                    String[] params = {diary};
                    String result = new saveDiary().execute(params).get();
                    if (result.equals("success")) {
                        Toast.makeText(getContext(), "Saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Oops!Something happened...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {

                }

            }
        });

        //Logout butonu
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isLogged", false);
                editor.putString("name", "");
                editor.putString("surname", "");
                editor.putString("email", "");
                editor.putString("fr", "");
                editor.putString(Constants.UNIQUE_ID,"none");
                editor.commit();

                Intent loginIntent = new Intent(getContext(), loginActivity.class);
                startActivity(loginIntent);
                getActivity().finish();
            }

        });

        //Paylaş butonu
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToWall();
            }
        });

        //Recommend butonu
        final String appLinkUrl = "https://fb.me/154257521647711";//developers.facebook.com dan alınan applink
        final String previewImageUrl = "https://pixabay.com/static/uploads/photo/2015/10/01/21/39/background-image-967820_960_720.jpg";//Uygulama resim urlsi

        btnRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(previewImageUrl)
                                    // .setPromotionDetails("Example Promotion","EXAMPLE")
                            .build();
                    AppInviteDialog.show(getActivity(), content);
                }

            }
        });

        //Contacts butonu
        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                        try {
                            ContactsFragment fr = new ContactsFragment();
                            fr.setArguments(getActivity().getIntent().getExtras());
                            getActivity().getSupportFragmentManager().beginTransaction()
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

        return rootView;
    }

    //Paylaş methodu
    private void shareToWall() {
        if (shareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle("My Diary")
                    .setContentDescription("Diary of " + name)
                    .setContentUrl(Uri.parse(Constants.SERVER_URL + "/ShowText.php?email=" + email))
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
            JSONObject jsonObject = jsonParser.makeHttpRequest(Constants.SERVER_URL + "/setDiary.php", "POST", args);

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
            pDialog = new ProgressDialog(getActivity());
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
            JSONObject jsonObject = jsonParser.makeHttpRequest(Constants.SERVER_URL + "/getDiary.php", "GET", args);

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
            pDialog = new ProgressDialog(getActivity());
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

    /*************************************************************************************************************/
    /***************************CONTACTS PERMISSIONS**************************************************************/
    /*************************************************************************************************************/
    public void getPermissionToReadUserContacts() {

        if (Build.VERSION.SDK_INT >= 23) {
            // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
            // checking the build version since Context.checkSelfPermission(...) is only available
            // in Marshmallow
            // 2) Always check for permission (even if permission has already been granted)
            // since the user can revoke permissions at any time through Settings
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // The permission is NOT already granted.
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.READ_CONTACTS)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},
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
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                // The permission is NOT already granted.
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.CALL_PHONE)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

                // Fire off an async request to actually get the permission
                // This will show the standard permission request dialog UI
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE},
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
                    Toast.makeText(getContext(), "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // showRationale = false if user clicks Never Ask Again, otherwise true
                    boolean showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);

                    if (showRationale) {
                        // do something here to handle degraded mode
                    } else {
                        Toast.makeText(getContext(), "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == CALL_PHONE_PERMISSIONS_REQUEST) {
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Call phone permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // showRationale = false if user clicks Never Ask Again, otherwise true
                    boolean showRationale = shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE);

                    if (showRationale) {
                        // do something here to handle degraded mode
                    } else {
                        Toast.makeText(getContext(), "Call phone permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    }
