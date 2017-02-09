package tv.superawesome.sacpidemo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONObject;

import tv.superawesome.lib.sacpi.install.SAInstall;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sasession.SASession;

public class SACPI_SAInstall_Async_Tests extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int TIMEOUT = 2500;

    public SACPI_SAInstall_Async_Tests() {
        super("tv.superawesome.sacpidemo", MainActivity.class);
    }

    @UiThreadTest
    @LargeTest
    public void testSAInstall_sendInstallEventToServer () {

        final SAInstall install = new SAInstall(getActivity());
        assertNotNull(install);

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

        // now that the click was generated
        // - skip the "checking" part since that's done in another test *and* we know the
        //   app that generated the click is "tv.superawesome.sacpidemo"
        // - send an install event with both the target & source packages and see if the
        //   ad server returns true, in that it recognizes a valid install
        final String targetPackage = "tv.superawesome.demoapp";
        final String sourcePackage = "tv.superawesome.sacpidemo";
        install.sendInstallEventToServer(targetPackage, sourcePackage, session, new SAInstall.SAInstallInterface() {
            @Override
            public void saDidCountAnInstall(boolean success) {
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
