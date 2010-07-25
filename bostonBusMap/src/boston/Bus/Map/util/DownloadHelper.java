package boston.Bus.Map.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;

/**
 * The Android people really recommend using the apache HTTPClient over the URL class. Not sure why exactly but it should usually be a
 * simple change
 * @author schneg
 *
 */
public class DownloadHelper {

	private final String url;
	
	private final HttpGet httpGet;
	private String httpResponse;
	
	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	
	public DownloadHelper(String url) {
		this.url = url;
		
		httpGet = new HttpGet(url);
	}

	public void connect() throws ClientProtocolException, IOException {
		httpResponse = httpClient.execute(httpGet, new BasicResponseHandler());
		
	}

	public String getResponseData()
	{
		return httpResponse;
	}
	
	@Override
	public String toString() {
		return url;
	}
}
