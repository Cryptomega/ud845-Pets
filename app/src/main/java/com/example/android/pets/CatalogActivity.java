package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
//import android.support.v4.widget.SimpleCursorAdapter;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    // loader ID
    private static final int PET_LOADER = 0;

    PetCursorAdapter myAdapter;

    /** Tag for the log messages */
    public static final String LOG_TAG = "PETSAPP/" + CatalogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // get ListView
        ListView myListView = (ListView) findViewById(R.id.pet_listview);

        // Set empty view
        myListView.setEmptyView(findViewById(R.id.empty_view));

        // initialize PetCursorAdapter with null cursor
        myAdapter = new PetCursorAdapter(this, null);

        // connect ListView to PetCursorAdapter
        myListView.setAdapter(myAdapter);

        // setup item click listener
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create intent to open Editor Activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Add URI itentifing entry to edit
                intent.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id));
                // launch the activity
                startActivity(intent);
            }
        });

        // initialize loader
        //getLoaderManager().initLoader(PET_LOADER, null, (android.app.LoaderManager.LoaderCallbacks<Cursor>) this);
        getSupportLoaderManager().initLoader(PET_LOADER, null, this);


    }

    @Override
    public void onStart()
    {
        super.onStart();
        //displayDatabaseInfo();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertPet()
    {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Gregory");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 13);

        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        Uri newPetUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);

        Toast.makeText(this, R.string.pet_saved, Toast.LENGTH_SHORT).show();
        Log.v("CatalogueActivity", "New Id: " + newPetUri.getLastPathSegment());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertPet();
                //displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* *********************************************
        ** PET LOADER CALLBACK METHODS *************
       ********************************************* */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case PET_LOADER:
                String[] projection = {
                        PetEntry._ID,
                        PetEntry.COLUMN_PET_NAME,
                        PetEntry.COLUMN_PET_BREED,
                        PetEntry.COLUMN_PET_GENDER,
                        PetEntry.COLUMN_PET_WEIGHT   };
                return new CursorLoader(this, PetEntry.CONTENT_URI, projection, null, null, null);
            default:
                return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        myAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        myAdapter.changeCursor(null);
    }
    /* *********************************************
        ***** END LOADER CALLBACK METHODS **********
         ********************************************* */
}