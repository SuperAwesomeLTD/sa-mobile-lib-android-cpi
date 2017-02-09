package tv.superawesome.sacpidemo;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONObject;

import tv.superawesome.lib.sacpi.referral.SAReferral;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sasession.SASession;

public class SACPI_SARefferal_Async_Tests extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int TIMEOUT = 2500;

    public SACPI_SARefferal_Async_Tests() {
        super("tv.superawesome.sacpidemo", MainActivity.class);
    }

    @UiThreadTest
    @LargeTest
    public void testSAReferral_onReceive () {


        // create a new session (staging) object
        final SASession session = new SASession(getActivity());
        session.setConfigurationStaging();

        // first generate a new click on the ad server coming from this app,
        // "tv.superawesome.sacpidemo" as if to install the "tv.superawesome.demoapp", which is
        // the target that was setup in the dashboard
        String clickUrl = session.getBaseUrl() + "/click";
        JSONObject clickQuery = SAJsonParser.newObject(new Object[]{
                "placement", 588,
                "sourceBundle", session.getPackageName(),
                "creative", 5778,
                "line_item", 1063,
                "ct", session.getConnectionType(),
                "sdkVersion", "0.0.0",
                "rnd", session.getCachebuster()
        });

        SANetwork network = new SANetwork();
        network.sendGET(getActivity(), clickUrl, clickQuery, new JSONObject(), null);
        sleep();

        // now after the click is sent pretend that the user installed the app alongside some
        // referral data (passed through an intent)
        // then check to see if the server can work with that
        Intent intent = new Intent();
        intent.putExtra("referrer", "utm_source=1&utm_campaign=1218&utm_term=1063&utm_content=5778&utm_medium=588");

        SAReferral referral = new SAReferral(getActivity());
        referral.sendReferralEvent(intent, new SAReferral.SAReferralInterface() {
            @Override
            public void saDidSendReferralData(boolean success) {
                assertTrue(success);
            }
        });
        sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            fail("Unexpected Timeout");
        }
    }
}
