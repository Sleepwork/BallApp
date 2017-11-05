package com.example.zanta.ballapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Zanta on 14/10/2017.
 */

public class MyArrayAdapter extends ArrayAdapter<Map<String,String>> {
    private final Context context;
    private String[] from;
    private int[] to;

    public MyArrayAdapter(Context context, ArrayList<Map<String,String>> values, String[] from, int[] to) {
        super(context, R.layout.cell_type1, values);
        this.context = context;
        this.from = from;
        this.to = to;
    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View cellView = convertView;
        if (cellView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cellView = inflater.inflate(R.layout.cell_type1, parent, false);
        }
        TextView pseudoTV = (TextView)cellView.findViewById(to[0]);
        TextView scoreTV = (TextView)cellView.findViewById(to[1]);
        TextView rankingTV = (TextView) cellView.findViewById(to[2]);
        Map<String, String> s = getItem(position);
        pseudoTV.setText(s.get(from[0]));
        scoreTV.setText(s.get(from[1]));
        rankingTV.setText(s.get(from[2]));

        return cellView;
    } // fin de la méthode getView
} // fin de la classe MyArrayAdapter
