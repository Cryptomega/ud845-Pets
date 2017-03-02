package com.example.android.pets;

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
    private static final int URI_LOADER = 0;

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

        // initialize PetCursorAdapter
        myAdapter = new PetCursorAdapter(this, null);

        // connect ListView to PetCursorAdapter
        myListView.setAdapter(myAdapter);

        // initialize loader
        //getLoaderManager().initLoader(URI_LOADER, null, (android.app.LoaderManager.LoaderCallbacks<Cursor>) this);
        getSupportLoaderManager().initLoader(URI_LOADER, null, this);


    }

    @Override
    public void onStart()
    {
        super.onStart();
        //displayDatabaseInfo();
    }

    /*   // ******* OLD DISPLAY CODE
    private void displayDatabaseInfo()
    {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
         String[] projection = {
                 PetEntry._ID,
                 PetEntry.COLUMN_PET_NAME,
                 PetEntry.COLUMN_PET_BREED,
                 PetEntry.COLUMN_PET_GENDER,
                 PetEntry.COLUMN_PET_WEIGHT   };

        // Filter results WHERE "title" = 'My Title'
        // All rows, so no selection arguements
        // String selection = FeedEntry.COLUMN_NAME_TITLE + " = ?";
        // String[] selectionArgs = { "My Title" };

        // How you want the results sorted in the resulting Cursor
        //String sortOrder =
        //        FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,  // fill our uri
                projection, null, null, null );


        // get ListView
        ListView myListView = (ListView) findViewById(R.id.pet_listview);

        // Set empty view
        myListView.setEmptyView(findViewById(R.id.empty_view));

        // connect PetCursorAdapter to cursor
        myAdapter = new PetCursorAdapter(this, cursor);

        // connect ListView to PetCursorAdapter
        myListView.setAdapter(myAdapter);
    }
    */

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
        ***** LOADER CALLBACK METHODS **************
       ********************************************* */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT   };
        switch (id)
        {
            case URI_LOADER:
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