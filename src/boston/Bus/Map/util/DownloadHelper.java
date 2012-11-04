package boston.Bus.Map.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.common.io.ByteStreams;

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
	
	private final DefaultHttpClient httpClient;
	
	public DownloadHelper(String url) {
		this.url = url;
		
		httpGet = new HttpGet(url);
		httpGet.addHeader("Accept-Encoding", "gzip");

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 30000);
		HttpConnectionParams.setSoTimeout(params, 30000);
		httpClient = new DefaultHttpClient(params);
	}

	public void connect() throws ClientProtocolException, IOException {
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity entity = httpResponse.getEntity();
		inputStream = entity.getContent();
		
		//if gzip is supported, decode the stream
		Header contentEncodingHeader = entity.getContentEncoding();
		if (contentEncodingHeader != null)
		{
			HeaderElement[] codecs = contentEncodingHeader.getElements();
			for (HeaderElement encoding : codecs)
			{
				if (encoding.getName().equalsIgnoreCase("gzip"))
				{
					inputStream = new GZIPInputStream(inputStream);
				}
			}
		}
		
		inputStream = new BufferedInputStream(inputStream, 4096);
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
