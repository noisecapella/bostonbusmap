package com.schneeloch.bostonbusmap_library.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by schneg on 1/7/18.
 */

public class Downloader implements IDownloader {
    class DownloadHelper implements IDownloadHelper {

        private final String url;

        private final HttpURLConnection urlConnection;
        private InputStream inputStream;

        public DownloadHelper(String url) {
            this.url = url;
            try {
                URL urlObject = new URL(url);
                LogUtil.i("url: " + url);

                urlConnection = (HttpURLConnection)urlObject.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream(), 4096);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void disconnect() {
            urlConnection.disconnect();
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

    @Override
    public IDownloadHelper create(String url) {
        return new DownloadHelper(url);
    }
}
