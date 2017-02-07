/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi;

import android.content.Context;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sanetwork.request.SANetworkInterface;
import tv.superawesome.lib.sasession.SASession;

/**
 * Class that abstracts away AwesomeAds way of making sure installs are more thoroughly checked.
 * What it does is send a message to the ad server requesting the number of potential apps that
 * could have generated a click to get the current app installed.
 * If the ad server returns a JSON array of potential apps, it then searches through the system
 * for them, and when it finds the first one, it returns via a callback.
 */
public class SASourceBundleInspector {

    // private member variables
    private SANetwork network;
    private Context context;
    private SASourceBundleInspectorInterface listener;

    /**
     * Constructor with context that initializes a SANetwork and listener objects
     *
     * @param context current context (activity or fragment)
     */
    SASourceBundleInspector(Context context) {
        this.network = new SANetwork();
        this.context = context;
        this.listener = new SASourceBundleInspectorInterface() {@Override public void saDidFindAppOnDevice(String sourceBundle) {}};
    }

    /**
     * Main method of the class that does the ad server check and returns the package name
     * that was found.
     *
     * @param session   a session to operate on
     * @param listener  an instance of the response listener, to send a callback
     */
    public void checkPackage(SASession session, final SASourceBundleInspectorInterface listener) {
        // get a reference of the listener
        this.listener = listener != null ? listener : this.listener;

        // create the URL
        String checkUrl = session.getBaseUrl() + "/checkinstall?bundle=" + session.getPackageName();

        network.sendGET(context, checkUrl, new JSONObject(), new JSONObject(), new SANetworkInterface() {
            /**
             * Once the GET request to /checkinstall?bundle=com.example.app is called, it's time
             * to process the answer and check which of the return package names is actually
             * on the device
             *
             * @param status    request status
             * @param payload   request string payload
             * @param success   request success
             */
            @Override
            public void saDidGetResponse(int status, String payload, boolean success) {

                // parse the new bundle Ids
                JSONArray bundleIds = SAJsonParser.newArray(payload);

                // the souce bundle Id destination
                String sourceBundleId = null;

                for (int i = 0; i < bundleIds.length(); i++) {
                    try {
                        String bundleId = bundleIds.getString(i);
                        boolean exists = isPackageInstalled(context, bundleId);
                        if (exists) {
                            sourceBundleId = bundleId;
                            break;
                        }
                    } catch (JSONException e) {
                        // do nothing
                    }
                }

                // send the result back
                SASourceBundleInspector.this.listener.saDidFindAppOnDevice(sourceBundleId);
            }
        });
    }

    /**
     * Method that checks to see if a package name is on the device
     *
     * @param context       the current context (fragment or activity)
     * @param packageName   the package name to search for on the device
     * @return              true or false
     */
    private boolean isPackageInstalled(Context context, String packageName) {
        // in case of null context, just return false
        if (context == null) return false;

        // get the package manager
        PackageManager packageManager = context.getPackageManager();

        // try getting package info
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Interface that defines a method to be implemented by the SASourceBundleInspector class
     */
    public interface SASourceBundleInspectorInterface {

        /**
         * Callback method that is called when the source bundle inspector finishes searching for
         * a package on the device
         *
         * @param sourceBundle the source bundle that's been found
         */
        void saDidFindAppOnDevice (String sourceBundle);
    }
}
