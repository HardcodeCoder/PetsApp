package com.hardcodecoder.petsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hardcodecoder.petsapp.data.PetContract.PetEntry;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class CatalogActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyPet() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, "7");
        getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        //SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        // Cursor cursor = db.rawQuery("SELECT * FROM " + PetEntry.TABLE_NAME, null);
        String[] projection = new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT};

        Cursor c = getContentResolver().query(PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);


        if(c != null) {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = findViewById(R.id.text_view_pet);
            displayView.setText("");
            displayView.append(getString(R.string.info));
            displayView.append(String.valueOf(c.getCount()));

            displayView.append("\n_id - name - breed - gender - weight");
            int idIndex = c.getColumnIndex(PetEntry._ID);
            int nameIndex = c.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedIndex = c.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderIndex = c.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightIndex = c.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            while (c.moveToNext()) {
                int id = c.getInt(idIndex);
                String name = c.getString(nameIndex);
                String breed = c.getString(breedIndex);
                int gender = c.getInt(genderIndex);
                int weight = c.getInt(weightIndex);

                displayView.append("\n" + id + "-" + name + "-" + breed + "-" + gender + "-" + weight);
            }
            c.close();
        }
    }
}
