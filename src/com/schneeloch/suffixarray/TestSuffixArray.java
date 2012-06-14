package com.schneeloch.suffixarray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class TestSuffixArray {
	private class Tuple implements ObjectWithString {
		private final int lineNum;
		private final String line;

		public Tuple(int wordNum, String line) {
			this.lineNum = wordNum;
			this.line = line;
		}

		@Override
		public String getString() {
			return line;
		}

		public int getLineNum() {
			return lineNum;
		}
	}

	public static void main(String[] args) {
		new TestSuffixArray().run(args);
	}
	
	public void run(String[] args) {
		if (args.length == 0) {
			args = new String[] {"test-data/pg2701.txt.gz"};

		}

		System.out.println("Reading test data...");
		try
		{
			InputStream in;
			try
			{
				GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(args[0]));
				in = gzipIn;
			}
			catch (IOException e) {
				// assuming it's not in gzip format, continue
				in = new FileInputStream(args[0]);
			}

			StringBuilder builder = new StringBuilder();
			byte[] buffer = new byte[4096];
			int len;
			while ((len = in.read(buffer, 0, buffer.length)) > 0) {
				builder.append(new String(buffer, 0, len));
			}
			
			System.out.println("Builder is length " + builder.length() + ". Splitting into lines...");
			String s = builder.toString();
			String[] lines = s.split("\n");
			System.out.println("Number of lines: " + lines.length);
			ArrayList<Tuple> tuples = new ArrayList<Tuple>();
			int i = 0;
			for (String line : lines) {
				Tuple tuple = new Tuple(i, line);
				tuples.add(tuple);
				i++;
			}
			
			System.out.println("Memory used before creating suffix array: " + getMemory());
			System.out.println("Creating suffix array from tuples...");
			SuffixArray suffixArray = new SuffixArray(true);
			for (Tuple tuple : tuples) {
				suffixArray.add(tuple);
			}
			System.out.println("Suffix array is size " + suffixArray.size() + ". Sorting suffix array...");
			suffixArray.sort();
			
			System.out.println("Memory used after suffix array is sorted: " + getMemory());
			
			String key = "leviathan";
			System.out.println("Searching for " + key + "...");
			for (ObjectWithString result : suffixArray.search(key)) {
				Tuple tuple = (Tuple)result;
				System.out.println("Line: " + tuple.getLineNum() + ", " + tuple.getString());
			}
			System.out.println("Ending memory: " + getMemory());

			System.out.println("Done!");
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

    private long getMemory() {
    	Runtime runtime = Runtime.getRuntime();
    	runtime.gc();
    	return runtime.totalMemory() - runtime.freeMemory();
	}
}
