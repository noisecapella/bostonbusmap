package com.schneeloch.bostonbusmap_library.test;

import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.util.IDownloadHelper;
import com.schneeloch.bostonbusmap_library.util.IDownloader;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by schneg on 1/7/18.
 */

public class TestingDownloader implements IDownloader {
    public final Map<String, InputStream> streams;

    public TestingDownloader(Map<String, InputStream> streams) {
        this.streams = streams;
    }

    @Override
    public IDownloadHelper create(final String url) {
        return new IDownloadHelper() {
            @Override
            public void disconnect() {
                // do nothing
            }

            @Override
            public InputStream getResponseData()
            {
                InputStream stream = streams.get(url);
                if (stream == null) {
                    throw new RuntimeException("Unable to find stream for " + url);
                }
                return stream;
            }
        };
    }
}
