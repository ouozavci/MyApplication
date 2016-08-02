package com.example.oguzhan.myapplication;

import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.oguzhan.jsonlib.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oguzhan on 28.07.2016.
 */
public class ContactsFragment extends android.support.v4.app.Fragment implements View.OnClickListener {


    public List<PersonInfo> list_items = new ArrayList<PersonInfo>();
    private ListViewAdapter listviewAdapter;
    private ProgressDialog progressDialog;
    private Button button_start;
    private ListView listview_contacts;
    public static boolean controlState = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        listview_contacts = (ListView) view.findViewById(R.id.listView_contacts);

        //listview_contacts.setItemsCanFocus(false);

        if (list_items.size() == 0) {   // liste dolu ise tekrardan Async Task çağırma

            new FetchAsyncTask().execute();

        } else {


        }


        listview_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {


                final PersonInfo personInfo = list_items.get(position);


                DetailsFragment detailsFragment = new DetailsFragment();
                android.support.v4.app.FragmentManager fm = getFragmentManager();
                android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
                Bundle args = new Bundle();
                args.putString("name", personInfo.getName());
                args.putString("phone_number", personInfo.getPhoneNumber());
                detailsFragment.setArguments(args);
                ft.replace(R.id.blank_fragment, detailsFragment);
                ft.addToBackStack("tag2");
                ft.commit();

            }
        });


        return view;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {  // geri tuşuna bastığımızda listview in aynen kalması için gerekli
        super.onActivityCreated(savedInstanceState);

        this.listview_contacts = ((ListView) getActivity().findViewById(R.id.listView_contacts));
        this.listview_contacts.setAdapter(this.listviewAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag(R.id.key_position);

        if (v.getId() == R.id.call_image) {   // arama butonu tıklandığı zaman

            callPerson(list_items.get(position).getPhoneNumber());

        } else if (v.getId() == R.id.msg_image) {   // sms butonu tıklandığı zaman

            sendSms(list_items.get(position).getPhoneNumber());

        }

    }

    private void sendSms(String phoneNumber) {  // sms gönder

        Intent msgIntent = new Intent(Intent.ACTION_VIEW);
        msgIntent.setData(Uri.parse("sms:" + phoneNumber));
        startActivity(msgIntent);
    }

    private void callPerson(String phoneNumber) { // telefon ara

        Intent phoneCallIntent = new Intent(Intent.ACTION_CALL);
        phoneCallIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(phoneCallIntent);
    }


    public class FetchAsyncTask extends AsyncTask<Void, Void, List<PersonInfo>> {


        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Yükleniyor...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<PersonInfo> doInBackground(Void... params) {

            ContentResolver contentResolver = getActivity().getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                    null, null, null);
            List<String> contacts = new ArrayList<>();
            List<String> numbers = new ArrayList<>();
            String checkPhoneQuery = "select phoneNumber from userinfo where phoneNumber='qwerty'";
            if (cursor.getCount() > 0) {

                while (cursor.moveToNext()) {

                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); // id ye göre eşleşme yapılacak
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); // telefonda kayıtlı olan ismi
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // telefon numarasına sahip ise if içine gir.
                        Cursor person_cursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);

                        while (person_cursor.moveToNext()) {
                            String person_phoneNumber = person_cursor.getString(person_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll(" ", "");
                            if(person_phoneNumber.startsWith("+9"))person_phoneNumber=person_phoneNumber.substring(2);
                            if (!contacts.contains(name + "/" + person_phoneNumber)) {
                                contacts.add(name + "/" + person_phoneNumber);
                                numbers.add(person_phoneNumber);
                                checkPhoneQuery += " OR phoneNumber='" + person_phoneNumber + "'";
                            }
                        }

                    /*    while (person_cursor.moveToNext()) {
                            String person_phoneNumber = person_cursor.getString(person_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll(" ", "");

                            if (!contacts.contains(name + "/" + person_phoneNumber)) {
                                list_items.add(new PersonInfo(name, person_phoneNumber, false)); // ismini ve telefon numarasını list içine at
                                contacts.add(name + "/" + person_phoneNumber);
                                numbers.add(person_phoneNumber);
                                checkPhoneQuery += " OR phoneNumber='" + person_phoneNumber + "'";
                            }
                        }*/
                        person_cursor.close();

                    }

                }
            }

            Log.i("phoneQuery", checkPhoneQuery);
            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("query", checkPhoneQuery));
            ArrayList<String> usingNumbers = new ArrayList<>();
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject obj = jsonParser.makeHttpRequest(MainActivity.serverUrl + "/checkPhones.php",
                        "POST",
                        args);
                JSONArray jsnNumbers = obj.getJSONArray("product");

                    for(int i=0;i<jsnNumbers.length();i++){
                        usingNumbers.add(jsnNumbers.getString(i));
                    }

                if (obj != null) Log.i("phoneResult", obj.toString());
                else Log.i("phoneResult", "null");
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            for (int i = 0; i < contacts.size(); i++) {
                boolean isUsing = usingNumbers.contains(contacts.get(i).split("/")[1]);
                list_items.add(new PersonInfo(contacts.get(i).split("/")[0], contacts.get(i).split("/")[1], isUsing));
            }


            Collections.sort(list_items);
            return list_items;
        }


        @Override
        protected void onPostExecute(List contactList) {

            listviewAdapter = new ListViewAdapter(getActivity(), contactList, ContactsFragment.this);
            listview_contacts.setAdapter(listviewAdapter);

            if (progressDialog.isShowing()) {

                progressDialog.dismiss();
            }

        }
    }
}
