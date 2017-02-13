/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import tv.superawesome.lib.sacpi.install.SAInstall;
import tv.superawesome.lib.sacpi.install.SAOnce;
import tv.superawesome.lib.sacpi.pack.SACheck;
import tv.superawesome.lib.sacpi.pack.SAPackage;
import tv.superawesome.lib.sacpi.referral.SAReferral;
import tv.superawesome.lib.sasession.SASession;

/**
 * Class that extends BroadcastReceiver in order to:
 * - if an app is installed via Google Play Store and there *is* referral data, then it can act
 * on it using the SAReferralEvent class
 * - otherwise, define a method called handleInstall that just sends an /install event to the
 * ad server and returns a callback indicating success or failure
 */
public class SACPI extends BroadcastReceiver {

    // SACPI instance variable that can be setup only once
    private static SACPI instance = new SACPI();

    /**
     * Private constructor that is only called once
     */
    public SACPI () {
        // do nothing
    }

    /**
     * Singleton method to get the only existing instance
     *
     * @return an instance of the SACPI class
     */
    public static  SACPI getInstance () {
        return instance;
    }

    /**
     * Main class method that handles all the aspects of properly sending an /install event
     * - verifying first that I can find a possible "installer" app on the user's device
     * - whatever the answer, send an /install event, with an additional "sourceBundle" parameter
     * attached.
     * This method also assumes a production session, so users won't have to set their own
     * session.
     *
     * @param context   current context (fragment or activity)
     * @param listener  return callback listener
     */
    public void handleInstall(final Context context, final SACPIInterface listener) {
        SASession session = new SASession(context);
        session.setConfigurationProduction();
        handleInstall(context, session, listener);
    }

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
    public void handleInstall(final Context context, final SASession session, final SACPIInterface listener) {
        handleInstall(context, session, session.getPackageName(), listener);
    }

    /**
     * Main class method that handles all the aspects of properly sending an /install event
     * - verifying first that I can find a possible "installer" app on the user's device
     * - whatever the answer, send an /install event, with an additional "sourceBundle" parameter
     * attached.
     *
     * @param context   current context (fragment or activity)
     * @param session   current session to be based on
     * @param target    the target you want to check & send an install for
     * @param listener  return callback listener
     */
    public void handleInstall(final Context context, final SASession session, final String target, final SACPIInterface listener) {

        final SACPIInterface _listener = listener != null ? listener : new SACPIInterface() {
            @Override public void saDidCountAnInstall(boolean success) {}};

        // create the objects I'll need for this method
        final SAOnce    once    = new SAOnce (context);
        final SACheck   check   = new SACheck (context);
        final SAPackage pack    = new SAPackage (context);
        final SAInstall install = new SAInstall (context);

        // find out if I've already sent the CPI event in a previous app session
        boolean isSent = once.isCPISent();

        // if not, go ahead
        if (!isSent) {

            // find out if the AwesomeAds server thinks there are a number of potential
            // apps (package names) that might've generated the install from this
            // device (actually truncated IP range)
            check.askServerForPackagesThatGeneratedThisInstall(target, session, new SACheck.SACheckInstallInterface() {
                @Override
                public void saDidGetListOfPackagesToCheck(List<String> packages) {

                    // find out if at least one of the apps the server sent is to be found
                    // on the user's device
                    String source = pack.findFirstPackageOnDeviceFrom(packages);

                    // and whatever the outcome, send an install event to the server
                    // and await for a response
                    install.sendInstallEventToServer(target, source, session, new SAInstall.SAInstallInterface() {
                        @Override
                        public void saDidCountAnInstall(boolean success) {
                            once.setCPISent();
                            _listener.saDidCountAnInstall(success);
                        }
                    });

                }
            });
        }
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

        SAReferral referral = new SAReferral(context);
        referral.sendReferralEvent(intent, null);
    }
}