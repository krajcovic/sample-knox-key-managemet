package cz.monetplus.knox.key_management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import cz.krajcovic.tokenlibrary.tms.TmsUtils;
import cz.krajcovic.tokenlibrary.utils.AppConfiguration;
import cz.monetplus.smartterminallibrary.tms.TmsConnection;
import cz.monetplus.smartterminallibrary.tms.TmsErrorCodes;
import cz.monetplus.smartterminallibrary.tms.TmsEvents;
import cz.monetplus.smartterminallibrary.tms.TmsUpdateTask;

import java.util.ArrayList;
import java.util.List;

public class FakeTmsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    private static final String TAG = FakeTmsActivity.class.getName();
    private static final String APP_FILE_DIR = "token_generator";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private FakeTmsActivity.UserLoginTask mAuthTask = null;

    private SharedPreferences preferences;

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private AppConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_tms);

        config = AppConfiguration.getInstance(getApplicationContext(), APP_FILE_DIR);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.etTmsUser);
        populateAutoComplete();

//        mPasswordView = (EditText) findViewById(R.id.etTmsPassword);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button btnUpdate = (Button) findViewById(R.id.btTmsUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
                attemptTmsUpdate();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        loadPreferences();
    }

    private void attemptTmsUpdate() {

        TmsConnection connection = getConnection();
        if(connection != null) {
            Log.i(TAG, connection.toString());

            TmsUpdateTask tmsTask = new TmsUpdateTask(this, connection, new TmsEvents() {
                @Override
                public void onProgress(String tag, final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onPostExecute(TmsErrorCodes result) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "TmsGetConfig result: " + result, Snackbar.LENGTH_LONG).show();

                    if (result == TmsErrorCodes.OK) {
                        EditText et = (EditText) findViewById(R.id.etTmsTtkKeyId);
                        Byte TTK_KEY_IX = Byte.valueOf(et.getText().toString());//14;

                        //PaxTools.saveKeys(TmsActivity.this, TTK_KEY_IX);
                    }

                }
            });
            tmsTask.execute();
        } else {
            Toast.makeText(this, "Cannot make connection, check a parameters", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPreferences() {
        EditText et = (EditText) findViewById(R.id.etTmsHost);
        et.setText(config.getSharedPreferences().getString(AppConfiguration.SHARED_TMS_URI, "172.25.68.43"));

        et = (EditText) findViewById(R.id.etTmsPort);
        et.setText(String.valueOf(config.getSharedPreferences().getInt(AppConfiguration.SHARED_TMS_PORT, 7440)));

        et = (EditText) findViewById(R.id.etTmsUser);
        et.setText(config.getSharedPreferences().getString(AppConfiguration.SHARED_TMS_TERM_ID, "PAXDKR01"));

        et = (EditText) findViewById(R.id.etTmsPassword);
        et.setText(config.getSharedPreferences().getString(AppConfiguration.SHARED_TMS_PASS, "xxx"));

        et = (EditText) findViewById(R.id.etTmsTtkKeyId);
        et.setText(String.valueOf(config.getSharedPreferences().getInt(AppConfiguration.SHARED_TMS_TTK_KEY_ID, 14)));
    }

    private TmsConnection getConnection() {

        EditText et = (EditText) findViewById(R.id.etTmsHost);
        String tmsUri = et.getText().toString(); //"172.25.68.43";

        et = (EditText) findViewById(R.id.etTmsPort);
        Integer tmsPort = Integer.valueOf(et.getText().toString()); //7440;

        et = (EditText) findViewById(R.id.etTmsUser);
        String tmsUser = et.getText().toString();//"PAXDKR01";

        et = (EditText) findViewById(R.id.etTmsPassword);
        String tmsPass = et.getText().toString();//"xxx";

        et = (EditText) findViewById(R.id.etTmsTtkKeyId);
        Byte TTK_KEY_IX = Byte.valueOf(et.getText().toString());//14;


        saveSettings(tmsUri, tmsPort, tmsUser, tmsPass, TTK_KEY_IX);

        TmsConnection connection = TmsUtils.getTmsConnection(APP_FILE_DIR, TTK_KEY_IX, tmsUri,
                tmsPort,
                tmsUser,
                tmsPass, getApplicationContext());

        return connection;
    }

    private void saveSettings(String uri, Integer port, String user, String pass, Byte ttkIndex) {
        SharedPreferences.Editor editor = config.getSharedPreferences().edit();

        editor.putString(AppConfiguration.SHARED_TMS_URI, uri);
        editor.putInt(AppConfiguration.SHARED_TMS_PORT, port);
        editor.putString(AppConfiguration.SHARED_TMS_TERM_ID, user);
        editor.putString(AppConfiguration.SHARED_TMS_PASS, pass);
        editor.putInt(AppConfiguration.SHARED_TMS_TTK_KEY_ID, ttkIndex);

        editor.commit();
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mUserView.setError(getString(R.string.error_invalid_email));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new FakeTmsActivity.UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), FakeTmsActivity.ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(FakeTmsActivity.ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(FakeTmsActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
