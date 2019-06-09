package com.hardcodecoder.petsapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hardcodecoder.petsapp.data.PetContract.PetEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private Uri uri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        uri = getIntent().getData();

        if(uri == null)
            setTitle("Add pet");


        else {
            setTitle("Edit pet");
            getSupportLoaderManager().initLoader(0, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(null == uri) insertPet();
                else updatePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertPet() {
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String weight = mWeightEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(weight));
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        if(null == newUri) Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();

    }

    private void updatePet(){
        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String weight = mWeightEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(weight));
        Uri u = ContentUris.withAppendedId(PetEntry.CONTENT_URI, ContentUris.parseId(uri)-1);
        int rows = getContentResolver().update(u, values, null, null);
        if(rows > 0) Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, R.string.update__not_successful, Toast.LENGTH_SHORT).show();
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
        data.moveToFirst();
        mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));
        mBreedEditText.setText(data.getString(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED)));
        mWeightEditText.setText(String.valueOf(data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT))));
        mGender = data.getInt(data.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER));
        int pos;
        pos = ((mGender == PetEntry.GENDER_MALE) ? 1 : ((mGender == PetEntry.GENDER_FEMALE) ? 2 : 0));
        mGenderSpinner.setSelection(pos);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mNameEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}

