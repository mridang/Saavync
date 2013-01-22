package com.mridang.saavync.authenticator;

import org.apache.commons.lang3.ArrayUtils;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.mridang.saavync.activities.AuthenticatorActivity;
import com.mridang.saavync.client.SaavnClient;
import com.mridang.saavync.others.Constants;

/*
 * This class contains the methods for the authentication system and the methods are invoked when
 * credentials and tokens are changed or requested.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    /* The */
    private final Context ctxContext;

    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#AbstractAccountAuthenticator(
     * android.content.Context)
     */
    public Authenticator(Context context) {

        super(context);
        ctxContext = context;

    }


    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#confirmCredentials(android
     * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
     * android.os.Bundle)
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {

        return null;

    }


    /*
     * @see android.accounts.AbstractAccountAuthenticator#editProperties(
     * android.accounts.AccountAuthenticatorResponse
     * , java.lang.String)
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {

        throw new UnsupportedOperationException();

    }


    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts
     * .AccountAuthenticatorResponse, java.lang.String, java.lang.String,
     * java.lang.String[], android.os.Bundle)
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options) {

        try {

            // If the caller wants to add an account type we don't support, then
            // return an error
            if (!accountType.equals(Constants.ACCOUNT_TYPE)) {
                Bundle bunBundle = new Bundle();
                bunBundle.putString(AccountManager.KEY_ERROR_MESSAGE,
                        "Invalid authentication token type requested.");
                return bunBundle;
            }

            // Extract the username and password from the Account Manager, and ask
            // the server for an appropriate AuthToken.
            AccountManager amrManager = AccountManager.get(ctxContext);
            Account[] accAccounts = amrManager.getAccountsByType(accountType);

            if (ArrayUtils.isEmpty(accAccounts)) {
                Intent ittIntent = new Intent(ctxContext, AuthenticatorActivity.class);
                ittIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                Bundle bunResult = new Bundle();
                bunResult.putParcelable(AccountManager.KEY_INTENT, ittIntent);
                return bunResult;
            }

            return null;

        } catch (Exception e) {
            return null;
        }

    }


    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#getAuthToken(android.accounts
     * .AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String, android.os.Bundle)
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse aarResponse, Account accAccount,
            String strType, Bundle bunOptions) throws NetworkErrorException {

        try {

            // If the caller requested an authToken type we don't support, then
            // return an error
            if (!strType.equals(Constants.AUTHTOKEN_TYPE)) {
                Bundle bunBundle = new Bundle();
                bunBundle.putString(AccountManager.KEY_ERROR_MESSAGE,
                        "Invalid authentication token type requested.");
                return bunBundle;
            }

            // Extract the username and password from the Account Manager, and ask
            // the server for an appropriate AuthToken.
            AccountManager amrManager = AccountManager.get(ctxContext);
            String strPassword = amrManager.getPassword(accAccount);
            String strToken = SaavnClient.authenticate(ctxContext, accAccount.name, strPassword);
            if (!TextUtils.isEmpty(strToken)) {
                Bundle bunResult = new Bundle();
                bunResult.putString(AccountManager.KEY_ACCOUNT_NAME, accAccount.name);
                bunResult.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                bunResult.putString(AccountManager.KEY_AUTHTOKEN, strToken);
                return bunResult;
            }

            return null;

        } catch (Exception e) {
            return null;
        }

    }


    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#hasFeatures(android.accounts
     * .AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String[])
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse aarResponse, Account accAccount,
            String[] strFeatures) {

        Bundle bunResult = new Bundle();
        bunResult.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return bunResult;

    }


    /*
     * @see android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(java.lang.String)
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {

        return null;

    }


    /*
     * @see
     * android.accounts.AbstractAccountAuthenticator#updateCredentials(android
     * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String, android.os.Bundle)
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) {

        return null;

    }

}