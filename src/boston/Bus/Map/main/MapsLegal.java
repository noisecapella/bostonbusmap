package boston.Bus.Map.main;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

import boston.Bus.Map.R;

/**
 * Created by georgeandroid on 9/7/15.
 */
public class MapsLegal extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_legal);

        TextView legalText = (TextView)findViewById(R.id.legal_text_view);
        String licenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
        legalText.setText(licenseInfo);
        legalText.setMovementMethod(new ScrollingMovementMethod());
    }
}
