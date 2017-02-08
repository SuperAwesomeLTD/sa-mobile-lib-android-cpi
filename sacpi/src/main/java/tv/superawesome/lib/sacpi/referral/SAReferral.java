/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi.referral;

import android.content.Context;

import org.json.JSONObject;

import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.samodelspace.SAReferralData;
import tv.superawesome.lib.sasession.SAConfiguration;
import tv.superawesome.lib.sasession.SASession;
import tv.superawesome.lib.sautils.SAUtils;

/**
 * Class that contains methods to handle when the app receives referral data from the google
 * play store.
 */
public class SAReferral {

    // currnt context
    private Context context;

    /**
     * Normal constructor with context
     *
     * @param context current context (activity or fragment)
     */
    public SAReferral (Context context) {
        this.context = context;
    }

    /**
     * Method that transforms a received string that supposedly contains referral data into
     * a proper JSON string and then into a new SAReferralData object
     *
     * @param data  the string data, something like utm_source=33&utm_campaign=3121 ...
     * @return      a new instance of SAReferralData
     */
    public SAReferralData parseReferralResponse (String data) {

        String referrer;

        referrer = data != null ? data : "";
        referrer = referrer.replace("=", " : ");
        referrer = referrer.replace("%3D", " : ");
        referrer = referrer.replace("\\&", ", ");
        referrer = referrer.replace("&", ", ");
        referrer = referrer.replace("%26", ", ");
        referrer = "{ " + referrer + " }";
        referrer = referrer.replace("utm_source", "\"utm_source\"");
        referrer = referrer.replace("utm_campaign", "\"utm_campaign\"");
        referrer = referrer.replace("utm_term", "\"utm_term\"");
        referrer = referrer.replace("utm_content", "\"utm_content\"");
        referrer = referrer.replace("utm_medium", "\"utm_medium\"");

        return new SAReferralData(referrer);

    }

    /**
     * Method that forms a new event dictionary from a referral data object
     *
     * @param data  an instance of SAReferralData
     * @return      a new JSONObject that contains that will be used by the ad server to
     *              record the event
     */
    public JSONObject getReferralCustomData (SAReferralData data) {

        return SAJsonParser.newObject(new Object[]{
                // "sdkVersion", SuperAwesome.getInstance().getSDKVersion(),
                "rnd", SAUtils.getCacheBuster(),
                "ct", SAUtils.getNetworkConnectivity(context).ordinal(),
                "data", SAUtils.encodeDictAsJsonDict(SAJsonParser.newObject(new Object[]{
                        "placement", data.placementId,
                        "line_item", data.lineItemId,
                        "creative", data.creativeId,
                        "type", "custom.referred_install"
                }))
        });
    }

    /**
     * Method that forms the referral url where the event will be sent to
     *
     * @param data  an instance of SAReferralData
     * @return      a new URL string to send the event to
     */
    public String getReferralUrl (SAReferralData data) {

        SASession session = new SASession(context);
        SAConfiguration configuration = SAConfiguration.fromValue(data.configuration);
        session.setConfiguration(configuration);

        JSONObject refEventDict = getReferralCustomData(data);

        return session.getBaseUrl() + "/event?" + SAUtils.formGetQueryFromDict(refEventDict);
    }

    /**
     * This will always send the referral header needed by the GET request
     *
     * @return  a standard GET request header as a JSONObject
     */
    public JSONObject getReferralHeader () {
        return SAJsonParser.newObject(new Object[]{
                "Content-Type", "application/json",
                "User-Agent", SAUtils.getUserAgent(context)
        });
    }
}
