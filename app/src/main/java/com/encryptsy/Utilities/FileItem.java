package com.encryptsy.Utilities;

import android.net.Uri;

/**
 * Created by Chris on 7/22/2015.
 */
public class FileItem {
    private String path;
    private Uri uri;

    public FileItem(String path, Uri uri)
    {
        this.path = path;
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public Uri getUri() {
        return uri;
    }
}
