package com.schneeloch.bostonbusmap_library.util;

/**
 * Created by schneg on 1/7/18.
 */

public interface IDownloader {
    IDownloadHelper create(String url);
}
