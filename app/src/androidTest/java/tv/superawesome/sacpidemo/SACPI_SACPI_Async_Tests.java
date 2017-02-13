package tv.superawesome.sacpidemo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONObject;

import tv.superawesome.lib.sacpi.SACPI;
import tv.superawesome.lib.sacpi.SACPIInterface;
import tv.superawesome.lib.sacpi.install.SAOnce;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sasession.SASession;

public class SACPI_SACPI_Async_Tests extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int TIMEOUT = 2500;

    public SACPI_SACPI_Async_Tests () {
        super("tv.superawesome.sacpidemo", MainActivity.class);
    }

    @UiThreadTest
    @LargeTest
    public void testSACPI_sendInstallEvent1 () {

        // create a new session (staging) object
        final SASession session = new SASession(getActivity());
        session.setConfigurationStaging();

        // always reset the CPI
        final SAOnce once = new SAOnce(getActivity());
        once.resetCPISent();

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

        // call the whole CPI process:
        // - as if I was running this from the app that's just been installed "tv.superawesome.demoapp",
        // - see if the main CPI method executes correctly against a new click
        SACPI.getInstance().handleInstall(getActivity(), session, "tv.superawesome.demoapp", new SACPIInterface() {
            @Override
            public void saDidCountAnInstall(boolean success) {
                assertTrue(success);
                assertTrue(once.isCPISent());
            }
        });
        sleep();
    }

    @UiThreadTest
    @LargeTest
    public void testSACPI_sendInstallEvent2 () {

        // create a new session (staging) object
        final SASession session = new SASession(getActivity());
        session.setConfigurationStaging();

        // always reset the CPI
        final SAOnce once = new SAOnce(getActivity());
        once.resetCPISent();

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

        // call the whole CPI process:
        // - as if I was running this from another app that's got nothing to do with
        //   "tv.superawesome.demoapp"
        // - see if the main CPI method executes correctly, and for this other app doesn't
        //   return "true", but "false"
        SACPI.getInstance().handleInstall(getActivity(), session, "some.other.app", new SACPIInterface() {
            @Override
            public void saDidCountAnInstall(boolean success) {
                assertFalse(success);
                assertTrue(once.isCPISent());
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
