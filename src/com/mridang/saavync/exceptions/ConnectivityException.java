package com.mridang.saavync.exceptions;

/*
 * This class is the custom exception which generally occurs when there
 * was an error trying to communicate with the server.
 */
public class ConnectivityException extends Exception {

    /*
     * The default generated serial verison identifier.
     */
    private static final long serialVersionUID = 2546453031400052247L;

    /*
     * Constructor
     */
    public ConnectivityException() {

        super();

    }

    /*
     * Constructor
     */
    public ConnectivityException(String strMessage, Throwable thrException) {

        super(strMessage, thrException);

    }

}
