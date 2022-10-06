package com.mct.logcatwindow.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mct.logcatwindow.model.TraceLevel;
import com.mct.logcatwindow.model.TraceObject;

import java.util.List;

public class SimpleTextAdapter extends BaseAdapter {
    private final List<TraceObject> data;
    private final Context context;

    public SimpleTextAdapter(List<TraceObject> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int position) {
        return this.data.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            TextView textView = new TextView(this.context);
            textView.setPadding(16, 4, 0, 4);
            convertView = textView;
            viewHolder = new ViewHolder();
            viewHolder.logView = textView;
            textView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) ((View) convertView).getTag();
        }

        TraceObject traceObject = (TraceObject) this.data.get(position);
        viewHolder.logView.setText("[" + traceObject.getTraceLevel() + "]   " + traceObject.getDate() + "   " + traceObject.getMessage());
        viewHolder.logView.setTextColor(-1);
        if (traceObject.getMessage().contains("[console]")) {
            viewHolder.logView.setTextColor(-16711936);
        }

        if (traceObject.getTraceLevel() == TraceLevel.ERROR) {
            viewHolder.logView.setTextColor(Color.parseColor("#ff4416"));
        }

        return (View) convertView;
    }

    static class ViewHolder {
        TextView logView;
    }
}
