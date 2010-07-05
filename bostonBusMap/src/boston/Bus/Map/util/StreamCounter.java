package boston.Bus.Map.util;

import java.io.IOException;
import java.io.InputStream;

import boston.Bus.Map.main.UpdateAsyncTask;

public class StreamCounter extends InputStream
{
	private int count;
	private final InputStream wrappedStream;
	private final UpdateAsyncTask publisher;
	private final byte[] byteSpace = new byte[1];
	private final int contentLength;
	private final String contentLengthMissingString;
	private final String prepend;
	
	public StreamCounter(InputStream wrappedStream, UpdateAsyncTask publisher, int contentLength,
			String contentLengthMissingString, String prepend)
	{
		this.wrappedStream = wrappedStream;
		this.publisher = publisher;
		this.contentLength = contentLength;
		this.contentLengthMissingString = contentLengthMissingString;
		this.prepend = prepend;
	}
	
	@Override
	public int read(byte[] b, int offset, int length) throws IOException {
		
		int len = wrappedStream.read(b, offset, length);
		count += len;
		
		publish(count);
		return len;
	}

	private void publish(int totalRead) {
		int totalReadInKb = totalRead >> 10;
		
		String msg;
		if (contentLength > 0)
		{
			msg = prepend + " " + totalReadInKb + "kb of " + (contentLength / 1024) + "kb";
		}
		else
		{
			if (contentLengthMissingString != null)
			{
				msg = prepend + " " + totalReadInKb + "kb " + contentLengthMissingString;
			}
			else
			{
				msg = prepend + " " + totalReadInKb + "kb";					
			}
		}

		publisher.publish(msg);
	}

	@Override
	public int read() throws IOException {
		int r = read(byteSpace, 0, 1);
		if (r == 1)
		{
			return byteSpace[0];
		}
		else
		{
			return -1;
		}
	}
	
	@Override
	public int available() throws IOException {
		return wrappedStream.available();
	}

	@Override
	public void close() throws IOException {
		wrappedStream.close();
	}
	@Override
	public boolean equals(Object o) {
		return wrappedStream.equals(o);
	}
	@Override
	public int hashCode() {
		return wrappedStream.hashCode();
	}
	
	@Override
	public void mark(int readlimit) {
		wrappedStream.mark(readlimit);
	}
	@Override
	public boolean markSupported() {
		return wrappedStream.markSupported();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	@Override
	public synchronized void reset() throws IOException {
		wrappedStream.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return wrappedStream.skip(n);
	}
	
	
}
