
package com.android.settings.deviceinfo;

import android.os.SystemProperties;

public class VersionUtils {
    public static String getXtendedVersion(){
        String buildType = SystemProperties.get("ro.xtended.display.version","");
	return buildType.equals("") ? "" : "MSM-Xtended" + "-" + buildType;
    }
}
