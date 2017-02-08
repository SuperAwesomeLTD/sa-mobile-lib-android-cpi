/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi.install;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class contains methods that make sure that the CPI code gets executed just once.
 */
public class SAOnce {

    // constants for the shared preferences file name and key
    private static final String FILE_NAME   = "SA_CPI_File";
    private static final String KEY         = "SA_CPI_Key";

    // preferences object
    private SharedPreferences preferences = null;

    /**
     * Normal constructor with context
     *
     * @param context current context (activity or fragment)
     */
    public SAOnce (Context context) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Method that checks if the local preferences objects contains the SA CPI Key.
     *
     * @return true if present, false otherwise
     */
    public boolean isCPISent () {
        return preferences.contains(KEY);
    }

    /**
     * Method that sets the CPI install event as being sent by putting "true" under the
     * local preferences KEY key.
     */
    public void setCPISent () {
        preferences.edit().putBoolean(KEY, true).apply();
    }

    /**
     * Aux method (used mostly for testing) that resets the CPI sent KEY in the shared
     * preferences.
     */
    public void resetCPISent () {
        preferences.edit().remove(KEY).apply();
    }
}
