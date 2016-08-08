package com.example.oguzhan.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class GameUsersAdapter extends ArrayAdapter<PersonInfo> {

    private OnClickListener clickListener;

    public GameUsersAdapter(Context context, List<PersonInfo> items,OnClickListener onClickListener ) {

        super(context, R.layout.game_user_list_item, items);

        this.clickListener = onClickListener;

    }




    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        final ViewHolder viewHolder;
        if(view == null){


            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.game_user_list_item, parent,false);

            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView)view.findViewById(R.id.txtName);
            viewHolder.txtNumber = (TextView)view.findViewById(R.id.txtNumber);

            viewHolder.btnInvite = (Button)view.findViewById(R.id.btnInvite);

            view.setTag(viewHolder);


        }else{

            viewHolder = (ViewHolder) view.getTag();

        }

        final PersonInfo personInfo = getItem(position);

        setClickListeners(viewHolder.btnInvite);

        setTagsToViews(viewHolder.btnInvite,position);


        viewHolder.txtName.setText(personInfo.getName());
        viewHolder.txtNumber.setText(personInfo.getPhoneNumber());

        viewHolder.btnInvite.setVisibility(personInfo.isUsing()?View.VISIBLE:View.INVISIBLE);



        return view;
    }

    private void setTagsToViews(View view, int position) {

        view.setTag(R.id.key_position, position);
    }

    private void setClickListeners(View view) {

        view.setOnClickListener(clickListener);
    }


    private static class ViewHolder {

        TextView txtName;
        TextView txtNumber;

        Button btnInvite;
    }
}