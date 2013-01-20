package com.mridang.saavync.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mridang.saavync.R;
import com.mridang.saavync.client.SaavnClient;
import com.mridang.saavync.others.Constants;

/*
 * This class contains the activity which displays login screen to the user so
 * that he can input his credentials.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private AccountManager mAccountManager;

    /* The instance of the login task so can cancel it if requested */
    private UserLoginTask mAuthTask = null;
    /* The instance of the progress dialog so we can dismiss it */
    private ProgressDialog mProgressDialog = null;

    private TextView tvwMessage;

    private String strPassword;

    private String strUsername;

    /*
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     *
     * @param  vewView  The Submit button for which this method is invoked
     */
    public void handleLogin(View vewView) {

        strUsername = ((EditText) findViewById(R.id.username)).getText().toString();
        strPassword = ((EditText) findViewById(R.id.password)).getText().toString();

        if (TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strPassword)) {
            tvwMessage.setText("Either username or the password is empty");
        } else {
            showProgress();
            mAuthTask = new UserLoginTask();
            mAuthTask.execute();
        }

    }


    /*
     * Called when response is received from the server for authentication
     * request.
     *
     * @param  strToken  The token returned by the authentication query
     */
    private void finishLogin(String strToken) {

        Account actAccount = new Account(strUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.addAccountExplicitly(actAccount, strPassword, null);
        ContentResolver.setSyncAutomatically(actAccount, "media", true);

        Intent ittIntent = new Intent();
        ittIntent.putExtra(AccountManager.KEY_ACCOUNT_NAME, strUsername);
        ittIntent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(ittIntent.getExtras());
        setResult(RESULT_OK, ittIntent);
        finish();

    }


    /*
     * @see android.accounts.AccountAuthenticatorActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        mAccountManager = AccountManager.get(this);
        setContentView(R.layout.credentials);
        Button btnLogin = (Button)findViewById(R.id.login);
        btnLogin.setText(R.string.login_ok_button);
        btnLogin.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View vewView) {
        		AuthenticatorActivity.this.handleLogin(vewView);
        	}
        });

        tvwMessage = (TextView) findViewById(R.id.message);

    }


    /*
     * Shows the progress UI for a lengthy operation.
     */
    @SuppressWarnings("deprecation")
    private void showProgress() {

        showDialog(0);

    }


    /*
     * Called when the authentication process completes
     *
     * @param  strToken  The authentication token returned by the server, or authentication failed.
     */
    public void onAuthenticationResult(String strToken) {

        boolean success = ((strToken != null) && (strToken.length() > 0));

        mAuthTask = null;
        hideProgress();

        if (success) {
            finishLogin(strToken);
        } else
            tvwMessage.setText("Invalid username or password.");

    }


    /*
     * Represents an asynchronous task used to authenticate a user against the
     * synchronisation service
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        /*
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(final String authToken) {

            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            onAuthenticationResult(authToken);

        }


        /*
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected String doInBackground(Void... params) {

            try {
                return SaavnClient.authenticate(AuthenticatorActivity.this.getApplicationContext(), strUsername, strPassword);
            } catch (Exception ex) {
                return null;
            }

        }


        /*
         * @see android.os.AsyncTask#onCancelled()
         */
        @Override
        protected void onCancelled() {

            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the activity to let it know.
            onAuthenticationCancel();

        }

    }


    /*
     *
     */
    public void onAuthenticationCancel() {

        mAuthTask = null;
        hideProgress();

    }


    /*
     * Hides the progress UI for a lengthy operation.
     */
    private void hideProgress() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

    }


    /*
     * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {

    	super.onCreateDialog(id, args);

        ProgressDialog pdgLogin = new ProgressDialog(this);
        pdgLogin.setMessage(getText(R.string.login_authenticating_message));
        pdgLogin.setIndeterminate(true);
        pdgLogin.setCancelable(true);
        pdgLogin.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                    mAuthTask.cancel(true);
            }
        });

        return mProgressDialog = pdgLogin;

    }

}