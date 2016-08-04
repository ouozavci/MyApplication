package com.example.oguzhan.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.oguzhan.jsonlib.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.jar.Attributes;

public class FromNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new makeitnone().execute();
        Intent intent = new Intent(FromNotification.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    ProgressDialog pDialog;

    class makeitnone extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FromNotification.this);
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

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);

            JSONParser jsonParser = new JSONParser();
            ArrayList<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("id", sharedPreferences.getString(Constants.UNIQUE_ID, "none")));

            JSONObject object = jsonParser.makeHttpRequest(Constants.SERVER_URL+"/makeitnone.php",
                    "POST",
                    args);

            return null;
        }
    }
}
