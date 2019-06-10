package com.hardcodecoder.petsapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hardcodecoder.petsapp.data.PetContract.PetEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    /**
     * uri received via intent
     */
    private Uri uri;


    /**
     * boolean to keep track whether or not pet has been edited
     */
    private boolean mPetHasChanged = false;

    private static final int PET_LOADER = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        uri = getIntent().getData();

        if(uri == null)
            setTitle(getString(R.string.insert_mode));


        else {
            setTitle(getString(R.string.edit_mode));
            getSupportLoaderManager().initLoader(PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mNameEditText.setOnTouchListener(mTouchListener);

        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mBreedEditText.setOnTouchListener(mTouchListener);

        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mWeightEditText.setOnTouchListener(mTouchListener);

        mGenderSpinner = findViewById(R.id.spinner_gender);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.array_gender_options,
                android.R.layout.simple_spinner_item);

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
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(uri == null){
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (null == uri) insertPet();
                else updatePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
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
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedAlertDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
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
        showUnsavedAlertDialog(discardButtonClickListener);
    }

    private void insertPet() {
        int weight;
        if(!TextUtils.isEmpty(mNameEditText.getText() )) {

            if(TextUtils.isEmpty(mWeightEditText.getText())) weight = 0;
            else weight = Integer.parseInt(mWeightEditText.getText().toString().trim());

            String name = mNameEditText.getText().toString().trim();
            String breed = mBreedEditText.getText().toString().trim();

            ContentValues values = new ContentValues();
            values.put(PetEntry.COLUMN_PET_NAME, name);
            values.put(PetEntry.COLUMN_PET_BREED, breed);
            values.put(PetEntry.COLUMN_PET_GENDER, mGender);
            values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (null == newUri)
                Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePet(){
        if(mPetHasChanged) {
            int weight;
            if (!TextUtils.isEmpty(mNameEditText.getText())) {

                if (TextUtils.isEmpty(mWeightEditText.getText())) weight = 0;
                else weight = Integer.parseInt(mWeightEditText.getText().toString().trim());

                String name = mNameEditText.getText().toString().trim();
                String breed = mBreedEditText.getText().toString().trim();

                ContentValues values = new ContentValues();
                values.put(PetEntry.COLUMN_PET_NAME, name);
                values.put(PetEntry.COLUMN_PET_BREED, breed);
                values.put(PetEntry.COLUMN_PET_GENDER, mGender);
                values.put(PetEntry.COLUMN_PET_WEIGHT, weight);
                long id = ContentUris.parseId(uri);
                Uri u = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                int rows = getContentResolver().update(u, values, null, null);
                if (rows > 0)
                    Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.update__not_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        return new CursorLoader(this,    // Parent class context
                uri,                             // Uri to query
                projection,                      // Columns to query for
                null,                   // No selection
                null,                // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() < 1)
            return;
        if(data.moveToFirst()) {
            mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));
            mBreedEditText.setText(data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED)));
            mWeightEditText.setText(String.valueOf(data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT))));
            mGender = data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
            int pos;
            pos = ((mGender == PetEntry.GENDER_MALE) ? 1 : ((mGender == PetEntry.GENDER_FEMALE) ? 2 : 0));
            mGenderSpinner.setSelection(pos);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mNameEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedAlertDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.unsaved_changes_dialog_msg);
        dialogBuilder.setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null)
                    dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(R.string.discard, discardButtonClickListener);

        // Create and show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                if(deletePet() > 0)
                    Toast.makeText(EditorActivity.this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(EditorActivity.this, "Error deleting pet", Toast.LENGTH_SHORT).show();
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
    private int deletePet() {
        if(uri != null)
            return getContentResolver().delete(uri, null, null);
        return 0;
    }
}

