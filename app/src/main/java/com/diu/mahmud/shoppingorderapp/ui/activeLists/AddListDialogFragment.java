package com.diu.mahmud.shoppingorderapp.ui.activeLists;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.diu.mahmud.shoppingorderapp.R;
import com.diu.mahmud.shoppingorderapp.model.ShoppingList;
import com.diu.mahmud.shoppingorderapp.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Adds a new shopping list
 */
public class AddListDialogFragment extends DialogFragment {
    String mEncodedEmail;
    EditText mEditTextListName;

    /**
     * Public static constructor that creates fragment and
     * passes a bundle with data into it when adapter is created
     */
    public static AddListDialogFragment newInstance(String encodedEmail) {
        AddListDialogFragment addListDialogFragment = new AddListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        addListDialogFragment.setArguments(bundle);
        return addListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_list, null);
        mEditTextListName = (EditText) rootView.findViewById(R.id.edit_text_list_name);

        /**
         * Call addShoppingList() when user taps "Done" keyboard action
         */
        mEditTextListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    addShoppingList();
                }
                return true;
            }
        });

           /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addShoppingList();
                    }
                });

        return builder.create();
    }

    /**
     * Add new active list
     */
    public void addShoppingList() {
        // Get the string that the user entered into the EditText and make an object with it
        // We'll use "Anonymous Owner" for the owner because we don't have user accounts yet
        String userEnteredName = mEditTextListName.getText().toString();
        //String owner = "Anonymous Owner";
        /**
         * If EditText input is not empty
         */
        if (!userEnteredName.equals("")) {

            /**
             * Create Firebase references
             */
            //Firebase userListsRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail);
            DatabaseReference listsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL_ACTIVE_LISTS);
            //final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            DatabaseReference newListRef = listsRef.push();
            /* Save listsRef.push() to maintain same random Id */
            final String listId = newListRef.getKey();
/**
 * Set raw version of date to the ServerValue.TIMESTAMP value and save into
 * timestampCreatedMap
 */
            HashMap<String, Object> timestampCreated = new HashMap<>();
            timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            /* Build the shopping list */
            ShoppingList newShoppingList = new ShoppingList(userEnteredName, mEncodedEmail,
                    timestampCreated);

            /* Add the shopping list */
            newListRef.setValue(newShoppingList);

            /* Close the dialog fragment */
            AddListDialogFragment.this.getDialog().cancel();
        }
    }
}
