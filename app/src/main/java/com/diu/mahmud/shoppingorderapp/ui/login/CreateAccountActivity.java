package com.diu.mahmud.shoppingorderapp.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.diu.mahmud.shoppingorderapp.R;
import com.diu.mahmud.shoppingorderapp.model.User;
import com.diu.mahmud.shoppingorderapp.ui.BaseActivity;
import com.diu.mahmud.shoppingorderapp.ui.MainActivity;
import com.diu.mahmud.shoppingorderapp.utils.Constants;
import com.diu.mahmud.shoppingorderapp.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private ProgressBar progressBar;
    //private Firebase mFirebaseRef;
    private DatabaseReference mFirebaseRef;

    private EditText mEditTextUsernameCreate, mEditTextEmailCreate;
    private String mUserName, mUserEmail, mPassword;
    private SecureRandom mRandom = new SecureRandom();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mChildEventListener;

    private String mUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /**
         * Create Firebase references
         */
        //mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_check_inbox));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {
        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString().toLowerCase();
        mPassword = new BigInteger(130, mRandom).toString(32);


        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);
        if (!validEmail || !validUserName) return;


        /*mEditTextUsernameCreate.setErrorEnabled(false);
        mEditTextEmailCreate.setErrorEnabled(false);*/

        //progressBar.setVisibility(view.VISIBLE);

        /**
         * Check that email and user name are okay
         */


        /**
         * If everything was valid show the progress dialog to indicate that
         * account creation has started
         */
        mAuthProgressDialog.show();

        /**
         * Create new user with specified email and password
         */
        mFirebaseAuth.createUserWithEmailAndPassword(mUserEmail, mPassword).addOnCompleteListener(CreateAccountActivity.this ,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOG_TAG,"createUserWithEmail:onComplete:" + task.isSuccessful());
                progressBar.setVisibility(View.GONE);
                // If sign in fails, Log the message to the LogCat. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.d(LOG_TAG,"Authentication failed." + task.getException());

                } else {
                    startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
                    finish();
                }

                mFirebaseAuth.sendPasswordResetEmail(mUserEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mAuthProgressDialog.dismiss();
                        Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CreateAccountActivity.this);
                        SharedPreferences.Editor spe = sp.edit();

                        /**
                         * Save name and email to sharedPreferences to create User database record
                         * when the registered user will sign in for the first time*/

                        spe.putString(Constants.KEY_SIGNUP_EMAIL, mUserEmail).apply();

                        /**
                         * Encode user email replacing "." with ","
                         * to be able to use it as a Firebase db key
                         */
                        createUserInFirebaseHelper();

                        /**
                         *  Password reset email sent, open app chooser to pick app
                         *  for handling inbox email intent*/

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        try {
                            startActivity(intent);
                            finish();
                        } catch (android.content.ActivityNotFoundException ex) {
                                     //User does not have any app to handle email
                        }
                    }
                });

            }
        });
        Toast.makeText(getApplicationContext(), "You are successfully Registered !!",
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper() {
        final String encodedEmail = Utils.encodeEmail(mUserEmail);
        //final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        final DatabaseReference userLocation = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /* If there is no user, make one */
                if (dataSnapshot.getValue() == null) {
                 /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(mUserName, encodedEmail, timestampJoined);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(LOG_TAG, getString(R.string.log_error_occurred) + firebaseError.getMessage());
            }
        });
    }

    private boolean isEmailValid(String email) {
        boolean isGoodEmail =
                (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid),
                    email));
            return false;
        }
        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }


    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
