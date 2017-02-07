/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sanetwork.request.SANetworkInterface;
import tv.superawesome.lib.sasession.SAConfiguration;
import tv.superawesome.lib.sasession.SASession;
import tv.superawesome.lib.sautils.SAUtils;

/**
 * Class that abstracts away sending install events to the ad server, checking the install is
 * counted correctly by it and then sending back a callback informing SDK users of this fact
 */
public class SAInstallEvent {

    // constants
    private static final String PREFERENCES = "MyPreferences";
    private static final String CPI_INSTALL = "CPI_INSTALL";

    // private members
    private Context context     = null;
    private SharedPreferences preferences = null;
    private SAInstallEventInterface listener    = null;

    /**
     * Constructor that saves a reference to the context and initializes member variables
     *
     * @param context current context (activity or fragment)
     */
    public SAInstallEvent (Context context) {
        this.context = context;
        this.listener = new SAInstallEventInterface() {@Override public void saDidCountAnInstall(boolean success) {}};
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Main class method
     *
     * @param session       a current session to operate on
     * @param sourceBundle  a source bundle string, which might be null; and is expected by the
     *                      ad server as part of the /install event
     * @param listener      reference to a listener to send responses back to users
     */
    public void sendEvent (SASession session, String sourceBundle, SAInstallEventInterface listener) {

        // get a listener and make sure it's not ever null
        this.listener = listener != null ? listener : this.listener;

        // form a Key that tells whether an install event was already sent
        String key = CPI_INSTALL + "_" + (session.getConfiguration() == SAConfiguration.PRODUCTION ? "PROD" : "STAG");

        // if the event hasn't yet been sent, then ...
        if (!preferences.contains(key)) {

            // make sure this runs **just** once!
            preferences.edit().putBoolean(key, true).apply();

            // create the cpi final URL
            String cpiUrl = session.getBaseUrl() + "/install?";
            cpiUrl += "bundle=" + session.getPackageName();
            cpiUrl += sourceBundle != null ? "&sourceBundle=" + sourceBundle : "";

            // and the specific event header
            JSONObject header = SAJsonParser.newObject(new Object[] {
                    "Content-Type", "application/json",
                    "User-Agent", SAUtils.getUserAgent(context)
            });

            // run the network code to send an event ...
            SANetwork network = new SANetwork();
            network.sendGET(context, cpiUrl, new JSONObject(), header, new SANetworkInterface() {
                /**
                 * Overridden saDidGetResponse method in which I check the response back from
                 * the ad server is an JSON in the form of {"success: true} and if it's not
                 * I send back false
                 *
                 * @param status    request status
                 * @param payload   request string payload
                 * @param success   request success
                 */
                @Override
                public void saDidGetResponse(int status, String payload, boolean success) {

                    // read the payload
                    //  in case of success it's a JSON { "success" : true }
                    //  in case of failure it usually null
                    JSONObject eventResponse = SAJsonParser.newObject(payload);

                    // get the success status
                    boolean eventSuccess = SAJsonParser.getBoolean(eventResponse, "success", false);

                    // call listener
                    SAInstallEvent.this.listener.saDidCountAnInstall(eventSuccess);

                }
            });

        }
    }

    /**
     * Interface defining a callback method to be called when an install was successfully counted
     */
    public interface SAInstallEventInterface {

        /**
         * Method to implement. It just "returns" a success callback parameter to indicate all is OK
         *
         * @param success whether the install counting operation was done OK by the ad server
         */
        void saDidCountAnInstall (boolean success);
    }
}
