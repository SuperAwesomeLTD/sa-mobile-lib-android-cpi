/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tv.superawesome.lib.sasession.SASession;

/**
 * Class that extends BroadcastReceiver in order to:
 * - if an app is installed via Google Play Store and there *is* referral data, then it can act
 * on it using the SAReferralEvent class
 * - otherwise, define a method called sendInstallEvent that just sends an /install event to the
 * ad server and returns a callback indicating success or failure
 */
public class SACPI extends BroadcastReceiver {

    /**
     * Main class method that handles all the aspects of properly sending an /install event
     * - verifying first that I can find a possible "installer" app on the user's device
     * - whatever the answer, send an /install event, with an additional "sourceBundle" parameter
     * attached.
     *
     * @param context   current context (fragment or activity)
     * @param session   current session to be based on
     * @param listener  return callback listener
     */
    public void sendInstallEvent (Context context, final SASession session, final SAInstallEvent.SAInstallEventInterface listener) {
        SASourceBundleInspector inspector = new SASourceBundleInspector(context);
        final SAInstallEvent installEvent = new SAInstallEvent(context);

        inspector.checkPackage(session, new SASourceBundleInspector.SASourceBundleInspectorInterface() {
            @Override
            public void saDidFindAppOnDevice(String sourceBundle) {
                installEvent.sendEvent(session, sourceBundle, listener);
            }
        });
    }

    /**
     * Overridden BroadcastReceiver method that only gets called when an app is installed from
     * the Google Play Store
     *
     * @param context   current context (fragment or activity)
     * @param intent    an intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        SAReferralEvent referralEvent = new SAReferralEvent(context, intent);
        referralEvent.sendEvent();
    }
}