package com.shane.android.videoplayer.util;

import org.cybergarage.upnp.Device;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 2016-10-23
 */

public class DLNAUtil {
	private static final String MEDIARENDER = "urn:schemas-upnp-org:device:MediaRenderer:1";

	/**
	 * Check if the device is a media render device
	 *
	 * @param device
	 * @return
	 */
	public static boolean isMediaRenderDevice(Device device) {
		if (device != null
				&& MEDIARENDER.equalsIgnoreCase(device.getDeviceType())) {
			return true;
		}

		return false;
	}
}
