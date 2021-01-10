package com.schneeloch.bostonbusmap_library.util;

import java.io.IOException;

/**
 * Quite frankly, there's no good way to do long term serialization on Android
 * @author schneg
 *
 */
public interface CanBeSerialized {
	void serialize(IBox blob) throws IOException;
	
	//implement a constructor taking Box
}
