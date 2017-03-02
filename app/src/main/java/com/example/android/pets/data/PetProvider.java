package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.pets.data.PetContract;



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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
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
    public Uri insert(Uri uri, ContentValues values) {
        return null;
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
