package com.example.oguzhan.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.oguzhan.jsonlib.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AskPhoneActivity extends AppCompatActivity {

    Button btnSubmit;
    Button btnLater;
    EditText txtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_phone);

        txtPhone = (EditText) findViewById(R.id.txtPhone);

        btnSubmit = (Button) findViewById(R.id.btnSubmitPhone);
        btnLater = (Button) findViewById(R.id.btnLater);
        final Intent i = new Intent(AskPhoneActivity.this, MainActivity.class);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                String email = pref.getString("email", null);
                String phone =  txtPhone.getText().toString();
                String[] args = {email,phone};
                new setPhone().execute(args);


                startActivity(i);
                finish();
            }
        });

        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(i);
                finish();
            }
        });

    }
    ProgressDialog pDialog;
    class setPhone extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... args) {
            JSONParser jsonParser = new JSONParser();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", args[0]));
            params.add(new BasicNameValuePair("phone", args[1]));

            JSONObject obj = jsonParser.makeHttpRequest(Constants.SERVER_URL+"/setPhoneNumber.php",
                    "POST",
                    params);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AskPhoneActivity.this);
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
