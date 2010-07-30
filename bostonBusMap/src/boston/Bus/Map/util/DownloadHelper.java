package boston.Bus.Map.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import org.apache.http.HttpEntity;
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
	private InputStream inputStream;
	
	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	
	public DownloadHelper(String url) {
		this.url = url;
		
		httpGet = new HttpGet(url);
	}

	public void connect() throws ClientProtocolException, IOException {
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity entity = httpResponse.getEntity();
		inputStream = entity.getContent();
	}

	public InputStream getResponseData()
	{
		return inputStream;
	}
	
	@Override
	public String toString() {
		return url;
	}
}
