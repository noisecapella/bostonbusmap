package boston.Bus.Map.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.drawable.Drawable;

/**
 * Quite frankly, there's no good way to do long term serialization on Android
 * @author schneg
 *
 */
public interface CanBeSerialized {
	void serialize(IBox blob) throws IOException;
	
	//implement a constructor taking Box
}
