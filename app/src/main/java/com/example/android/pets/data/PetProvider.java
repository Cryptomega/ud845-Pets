package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;


import com.example.android.pets.data.PetContract.PetEntry;

import static android.content.ContentUris.parseId;


/**
 * Created by Philip on 3/1/2017.
 */

public class PetProvider extends ContentProvider
{

    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private PetDBHelper mDbHelper;

    // Create a UriMatcher static opbject
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {       // calls to addURI go here,  to establish Uri patterns
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        //  Create and initialize a PetDbHelper object to gain access to the pets database.
        mDbHelper = new PetDBHelper(getContext());
        return false;
    }

    // Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Get readable SQL database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                cursor = db.query(
                        PetEntry.TABLE_NAME,            // The table to query
                        projection,                     // The columns to return
                        selection,                      // The columns for the WHERE clause
                        selectionArgs,                  // The values for the WHERE clause
                        null,                           // don't group the rows
                        null,                         // don't filter by row groups
                        sortOrder  );
                break;
            case PET_ID:
                // perform query
                selection = PetEntry._ID + " =?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(
                        PetEntry.TABLE_NAME,            // The table to query
                        projection,                     // The columns to return
                        selection,                      // The columns for the WHERE clause
                        selectionArgs,                  // The values for the WHERE clause
                        null,                           // don't group the rows
                        null,                         // don't filter by row groups
                        sortOrder  );
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri");
        }

        return cursor;
    }

    //  * Returns the MIME type of data for the content URI.
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


    // Insert new data into the provider with the given ContentValues.
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final int match = sUriMatcher.match((uri));
        switch (match)
        {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(uri,newRowId);
    }

    // * Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    //   * Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
