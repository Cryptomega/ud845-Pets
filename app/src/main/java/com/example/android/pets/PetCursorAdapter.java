package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;
import com.example.android.pets.data.PetContract;

/**
 * Created by Philip on 3/2/2017.
 */

public class PetCursorAdapter extends CursorAdapter {
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // create and return new blank list item
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Populate list item view with pet data
        TextView nameView = (TextView)view.findViewById(R.id.name);
        String name = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME));
        //if ( name == null || name.equals("")) name = "<filler>";
        nameView.setText(name);

        TextView summaryView = (TextView)view.findViewById(R.id.summary);
        String breed = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED));
        //if ( breed == null || breed.equals("")) breed = "<filler>";
        summaryView.setText(breed);

    }
}
