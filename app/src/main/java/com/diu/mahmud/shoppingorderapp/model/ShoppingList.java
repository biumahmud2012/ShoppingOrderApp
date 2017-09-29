package com.diu.mahmud.shoppingorderapp.model;

import com.diu.mahmud.shoppingorderapp.utils.Constants;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;

/**
 * Defines the data structure for both Active and Archived ShoppingList objects.
 */

public class ShoppingList {
    private String listName;
    private String owner;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    //private HashMap<String, Object> timestampLastChangedReverse;
    private HashMap<String, User> usersShopping;

    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    /**
     * Use this constructor to create new ShoppingLists.
     * Takes shopping list listName and owner. Set's the last
     * changed time to what is stored in ServerValue.TIMESTAMP
     *
     * @param listName
     * @param owner
     */
    public ShoppingList(String listName, String owner, HashMap<String, Object> timestampCreated) {
        this.listName = listName;
        this.owner = owner;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
        this.usersShopping = new HashMap<>();
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    @Exclude
    public long getTimestampLastChangedLong() {

        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @Exclude
    public long getTimestampCreatedLong() {
        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public HashMap getUsersShopping() {
        return usersShopping;
    }

}
