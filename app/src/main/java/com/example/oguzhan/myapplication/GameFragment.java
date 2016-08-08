package com.example.oguzhan.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class GameFragment extends Fragment {

    Firebase mRootRef;

    TextView text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        Firebase.setAndroidContext(getContext());
        mRootRef = new Firebase(Constants.FIREBASE_APP);

        text = (TextView) rootView.findViewById(R.id.textGame);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Firebase messagesRef = mRootRef.child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.getValue(String.class);
                text.setText(message);
                Log.i("MessageFromFirebase",message);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
}