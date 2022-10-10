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
        return position;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            TextView textView = new TextView(this.context);
            textView.setPadding(16, 8, 0, 8);
            convertView = textView;
            viewHolder = new ViewHolder();
            viewHolder.logView = textView;
            textView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TraceObject traceObject = this.data.get(position);
        viewHolder.logView.setText("[" + traceObject.getTraceLevel() + "]   " + traceObject.getDate() + "\n" + traceObject.getMessage());
        viewHolder.logView.setTextColor(-1);

        color:
        {
            if (traceObject.getTraceLevel() == TraceLevel.WARNING) {
                viewHolder.logView.setTextColor(Color.parseColor("#ffb22f"));
                break color;
            }
            if (traceObject.getTraceLevel() == TraceLevel.INFO) {
                viewHolder.logView.setTextColor(Color.parseColor("#6ecfff"));
                break color;
            }
            if (traceObject.getTraceLevel() == TraceLevel.ERROR) {
                viewHolder.logView.setTextColor(Color.parseColor("#ff4416"));
            }
        }
        return convertView;
    }

    static class ViewHolder {
        TextView logView;
    }
}
