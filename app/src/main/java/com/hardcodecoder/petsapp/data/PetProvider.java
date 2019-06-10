package com.hardcodecoder.petsapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hardcodecoder.petsapp.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    private PetDbHelper mDbHelper;
    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Cursor that will hold the result of the query
        Cursor c;

        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                c = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                c = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        if(getContext() != null)
            c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        if(match == PETS) return insertPet(uri, values);
        else throw new IllegalArgumentException("Insertion is not supported for " + uri);

    }

    private Uri insertPet(Uri uri, ContentValues values){
        if(isDataValid(values)) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            long id = db.insert(PetEntry.TABLE_NAME, null, values);
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }
            // Once we know the ID of the new row in the table,
            // return the new URI with the ID appended to the end of it

            //Notify
            if(getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch(match){
            case PETS:
                //Delete all pets that matches the selection and selection args
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                //Deletes only one pet that matches the id
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException();
        }

        if(rowsDeleted > 0 && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if(values != null) {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case PETS:
                    return updatePet(uri, values,selection, selectionArgs);
                case PET_ID:
                    // For the PET_ID code, extract out the ID from the URI,
                    // so we know which row to update. Selection will be "_id=?" and selection
                    // arguments will be a String array containing the actual ID.
                    selection = PetEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updatePet(uri, values, selection, selectionArgs);

                default:
                    throw new IllegalArgumentException("Update is not supported for \" + uri");
            }
        }
        return 0;
    }

    private int updatePet(Uri uri, ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs){
        if(values.size() == 0)
            return 0;

        if(isDataValid(values)){
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
            if(rowsUpdated > 0 ){
                if(getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }
        return 0;
    }

    private boolean isDataValid(ContentValues values){
        if(!PetEntry.isValidGender(values.getAsInteger(PetEntry.COLUMN_PET_GENDER))) {
            Log.e(LOG_TAG, "Please specify a valid gender");
            return false;
        }

        if(values.getAsString(PetEntry.COLUMN_PET_NAME) == null) {
            Log.e(LOG_TAG, "Please enter a valid name");
            return false;
        }
        return true;
    }
}
