package tv.superawesome.sacpidemo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import tv.superawesome.lib.sacpi.pack.SACheck;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.sanetwork.request.SANetworkInterface;
import tv.superawesome.lib.sasession.SASession;
import tv.superawesome.lib.sautils.SAUtils;

public class SACPI_SACheck_Async_Tests extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int TIMEOUT = 2500;

    public SACPI_SACheck_Async_Tests() {
        super("tv.superawesome.sacpidemo", MainActivity.class);
    }

    @UiThreadTest
    @LargeTest
    public void testSACheck_askServerForPackagesThatGeneratedThisInstall () {

        // create a new check object
        final SACheck check = new SACheck(getActivity());
        assertNotNull(check);

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

        // after the click has been executed, check to see that for "tv.superawesome.demoapp"
        // the ad server will return an array of one possible app, this one,
        // "tv.superawesome.sacpidemo", that has just triggered the click
        final String targetPackage = "tv.superawesome.demoapp";
//        final List<String> expectedPackages = Collections.singletonList("tv.superawesome.sacpidemo");
//        final String expectedPackage = "tv.superawesome.sacpidemo";

        check.askServerForPackagesThatGeneratedThisInstall(targetPackage, session, new SACheck.SACheckInstallInterface() {
            @Override
            public void saDidGetListOfPackagesToCheck(List<String> packages) {

                // test assumptions
                assertNotNull(packages);
                assertFalse(packages.isEmpty());
                assertTrue(packages.size() > 0);

//                String firstPackage = packages.get(0);
//                assertNotNull(firstPackage);
//                assertEquals(expectedPackage, firstPackage);

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
