package com.sharpsec.fmw;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sharpsec.fmw.location.indoors.LocationEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventListAdapter extends ArrayAdapter<LocationEvent> {

    public EventListAdapter(Context context, ArrayList<LocationEvent> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent,
                    false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LocationEvent item = getItem(position);
        if(item != null) {
            String time = new SimpleDateFormat("HH:mm:ss.SSS").format(item.getTime());
            String text = String.format("%s  %s  %s", item.getId(), time, item.getAction());
            holder.text.setText(text);
        }

        return convertView;
    }

    public class ViewHolder {
        TextView text;
    }
}
