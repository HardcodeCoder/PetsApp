package com.hardcodecoder.petsapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.hardcodecoder.petsapp.data.PetContract.PetEntry;

public class PetsCursorAdapter extends CursorAdapter {


    PetsCursorAdapter(Context context, Cursor c){
        super(context,c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pets_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = view.findViewById(R.id.name);
        tv.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));
        tv = view.findViewById(R.id.summary);
        String s = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED));
        if(TextUtils.isEmpty(s))
            s = "unknown breed";
        tv.setText(s);
    }
}
