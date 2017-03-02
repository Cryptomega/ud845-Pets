package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by Philip on 3/1/2017.
 */

public final class PetContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PetContract() { }

    public static class PetEntry implements BaseColumns {
        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        public static final Integer GENDER_MALE = 1;
        public static final Integer GENDER_FEMALE = 2;
        public static final Integer GENDER_UNKONWN = 0;


    }
}

