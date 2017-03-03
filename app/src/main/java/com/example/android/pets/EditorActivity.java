/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDBHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int PET_FIELD_LOADER = 1;
    private Uri intentUri;

    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    /** Tag for the log messages */
    public static final String LOG_TAG = "PETSAPP/" + EditorActivity.class.getSimpleName();

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // use getIntent() and getData() to get associated URI
        // if URI includes pet to edit, set title to "Edit Pet"
        // and populate fields
        intentUri = null;
        Intent intent = getIntent();
        intentUri = intent.getData();

        if  (intentUri == null)
        {
            // No intent, so we are adding a new pet
            setTitle(R.string.editor_activity_title_new_pet);
            invalidateOptionsMenu(); // so we can remove "Delete" option
        }
        else
        {    // Launched with intent uri, so we are updating a pet
            setTitle(R.string.editor_activity_title_edit_pet);

            // Show URI in Toast message for debugging
            Toast.makeText(this, intentUri.toString(), Toast.LENGTH_SHORT).show();

            getSupportLoaderManager().initLoader(PET_FIELD_LOADER, null, this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        // Listen for changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKONWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (intentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                try {
                    savePetToDb();
                    finish();
                } catch (Exception ex) {
                    Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG,ex.getMessage());
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePetToDb()
    {
        int weight = 0;
        if ( !TextUtils.isEmpty( mWeightEditText.getText().toString() ))
            weight = Integer.parseInt( mWeightEditText.getText().toString() );
        //try { weight = Integer.parseInt(mWeightEditText.getText().toString().trim()); }
        //catch(Exception ex) {}

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, weight);


        if  (intentUri == null)     // no intent passed, we are adding new pet
        {
            Uri newPetUri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);
            if (newPetUri != null )
            {
                Toast.makeText(this,R.string.pet_saved,Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else                        // we are updating an existing pet
        {
            int rowsUpdated = getContentResolver().update(intentUri, values, null, null);
            if (rowsUpdated > 0)
            {
                Toast.makeText(this,R.string.pet_updated,Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Otherwise the operation has failed, show alert message
        Toast.makeText(this,R.string.editor_insert_pet_failed,Toast.LENGTH_SHORT).show();
    }

    // ***************************************************
    // ***  Cursor Loader Callback functions *************
    // ***************************************************
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {

        switch (id)
        {
            case PET_FIELD_LOADER:
                String[] projection = {
                        PetContract.PetEntry._ID,
                        PetContract.PetEntry.COLUMN_PET_NAME,
                        PetContract.PetEntry.COLUMN_PET_BREED,
                        PetContract.PetEntry.COLUMN_PET_GENDER,
                        PetContract.PetEntry.COLUMN_PET_WEIGHT   };
                return new CursorLoader(this, intentUri, projection, null, null, null);
            default:
                return null;
        }

        //return null; // debugging
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        // update UI with pet info
        if ( cursor.moveToFirst() )
        {
            String name = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME));
            String breed = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED));
            int gender = cursor.getInt(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER));
            Integer weight = cursor.getInt(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT));

            //** EditText field to enter the pet's name
            mNameEditText.setText(name);

            //** EditText field to enter the pet's breed
            mBreedEditText.setText(breed);

            //** EditText field to enter the pet's weight
            mWeightEditText.setText(weight.toString());

            //** EditText field to enter the pet's gender
            mGenderSpinner.setSelection(gender);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // Clear input fields
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("0");
        mGenderSpinner.setSelection(PetContract.PetEntry.GENDER_UNKONWN);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
    {
        // Create an AlertDialog.Builder to prompt if user wants to leave without saving
        // Set click listeners to get response
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard,discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Keep editing"
                if (dialog != null )
                    dialog.dismiss();
            }
        });

        // Create and show AlertDialog
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed()
    {
        // continue with 'back' action if pet hasnt changed
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        if ( intentUri == null )
            return; //nothing to delete

        int numDeleted = getContentResolver().delete(intentUri, null, null);
        if ( numDeleted > 0 )
            Toast.makeText(this,R.string.editor_delete_pet_successful,Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,R.string.editor_delete_pet_failed,Toast.LENGTH_SHORT).show();
        finish();

    }

}