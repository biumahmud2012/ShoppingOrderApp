package com.diu.mahmud.shoppingorderapp;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ShoppingListApplication extends android.app.Application {

    public void onCreate() {
        super.onCreate();
        /* Initialize Firebase */
        //Firebase.setAndroidContext(this);
        /* Enable disk persistence  */
        //Firebase.getDefaultConfig().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}