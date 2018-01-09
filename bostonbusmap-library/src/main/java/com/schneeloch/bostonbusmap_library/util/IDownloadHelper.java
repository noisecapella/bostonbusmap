package com.schneeloch.bostonbusmap_library.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schneg on 1/7/18.
 */

public interface IDownloadHelper {
    public void disconnect();

    public InputStream getResponseData();

}
