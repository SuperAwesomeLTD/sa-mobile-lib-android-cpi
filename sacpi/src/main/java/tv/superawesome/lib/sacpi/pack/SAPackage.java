/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sacpi.pack;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * This class holds methods that:
 * - check if a package is located on a device
 * - find the first existing package from a given array of packages (given as a list or json array)
 *
 */
public class SAPackage {

    // local reference to the context
    private Context context;

    /**
     * Normal constructor with context
     *
     * @param context current context (activity or fragment)
     */
    public SAPackage(Context context) {
        this.context = context;
    }

    /**
     * Method that returns the first package actually found on the device from a list of given
     * possible packages
     *
     * @param potentialPackages potential packages as a List of strings
     * @return                  the first one found, if any
     */
    public String findFirstPackageOnDeviceFrom (List<String> potentialPackages) {

        try {
            // go through all the potential packages in the JSONArray and find the first one
            for (String packageName : potentialPackages) {
                if (isPackageOnDevice(packageName)) {
                    return packageName;
                }
            }
        }
        catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Method that checks to see if a package name is on the device
     *
     * @param packageName   the package name to search for on the device
     * @return              true or false
     */
    public boolean isPackageOnDevice (String packageName) {

        // get the package manager
        PackageManager packageManager;

        try {
            packageManager = context.getPackageManager();
        } catch (Exception e) {
            return false;
        }

        // try getting package info
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
