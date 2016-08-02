package com.example.oguzhan.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ListViewAdapter extends ArrayAdapter<PersonInfo> {

    private OnClickListener clickListener;

    public ListViewAdapter(Context context, List<PersonInfo> items,OnClickListener onClickListener ) {


        super(context, R.layout.list_view_item, items);

        this.clickListener = onClickListener;

    }




    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        final ViewHolder viewHolder;
        if(view == null){


            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.list_view_item, parent,false);

            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView)view.findViewById(R.id.txtName);
            viewHolder.txtNumber = (TextView)view.findViewById(R.id.txtNumber);
            viewHolder.txtUsing = (TextView)view.findViewById(R.id.txtUsing);
            viewHolder.imgCall = (ImageButton)view.findViewById(R.id.call_image);
            viewHolder.imgMsg = (ImageButton)view.findViewById(R.id.msg_image);
            viewHolder.imgCall.setTag(position);
            viewHolder.imgMsg.setTag(position);
            view.setTag(viewHolder);


        }else{

            viewHolder = (ViewHolder) view.getTag();


        }

        final PersonInfo personInfo = getItem(position);


        setClickListeners(viewHolder.imgCall);  // arama imagebuttona tıklanma eventi ver
        setClickListeners(viewHolder.imgMsg);   // mesaj gönderme imagebuttona tıklanma eventi ver

        setTagsToViews(viewHolder.imgCall, position);  // arama imagebuttona tag olarak position ver
        setTagsToViews(viewHolder.imgMsg, position);  // mesaj gönderme imagebuttona tag olarak position ver



        viewHolder.imgMsg.setTag(position);
        viewHolder.imgCall.setTag(position);
        viewHolder.txtName.setText(personInfo.getName());
        viewHolder.txtNumber.setText(personInfo.getPhoneNumber());
        viewHolder.txtUsing.setText(personInfo.isUsing()?"using loginApp!":"");



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
        TextView txtUsing;
        ImageButton imgCall;
        ImageButton imgMsg;
    }
}