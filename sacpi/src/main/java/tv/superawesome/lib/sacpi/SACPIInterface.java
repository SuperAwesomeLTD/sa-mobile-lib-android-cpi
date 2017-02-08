/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi;

/**
 * Interface defining a callback method to be called when an install was successfully counted
 */
public interface SACPIInterface {

    /**
     * Method to implement. It just "returns" a success callback parameter to indicate all is OK
     *
     * @param success whether the install counting operation was done OK by the ad server
     */
    void saDidCountAnInstall (boolean success);
}
