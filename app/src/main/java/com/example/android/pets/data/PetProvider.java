package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;


import com.example.android.pets.data.PetContract.PetEntry;

import java.util.Objects;

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
    public static final String LOG_TAG = "PETSAPP/" + PetProvider.class.getSimpleName();

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
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
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
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if ( name == null || name.equals("") )
            throw new IllegalArgumentException("Name cannot be null!");
        int gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender != PetEntry.GENDER_FEMALE &&
                gender != PetEntry.GENDER_MALE &&
                gender != PetEntry.GENDER_UNKONWN )
            throw new IllegalArgumentException("Invalid Gender");
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if ( weight != null && weight < 0 )
            throw new IllegalArgumentException("Weight cannot be less than zero!");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        if ( newRowId == -1 )
        {
            Log.e(LOG_TAG,"Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri,newRowId);
    }

    // * Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (match)
        {
            case PETS:
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }


    //   * Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {

        // check name
        if (values.containsKey(PetEntry.COLUMN_PET_NAME))
            if (values.getAsString(PetEntry.COLUMN_PET_NAME) == null)
                throw new IllegalArgumentException("Pet requires a name");

        // check weight
        if ( values.containsKey(PetEntry.COLUMN_PET_WEIGHT) )
            if ( values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) < 0
                    && values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) != null )
                throw new IllegalArgumentException("Weight cannot be less than zero!");

        // check gender
        if ( values.containsKey(PetEntry.COLUMN_PET_GENDER) )
        {
            int gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender != PetEntry.GENDER_FEMALE &&
                    gender != PetEntry.GENDER_MALE &&
                    gender != PetEntry.GENDER_UNKONWN )
                throw new IllegalArgumentException("Invalid gender!");
        }

        // check size of ContentValues
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0)
            return 0;


        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
