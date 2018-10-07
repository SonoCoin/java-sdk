package io.sonocoin.sdk.Sound;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;

/**
* Created by sciner on 15.10.2017.
*/
public class FileProvider extends android.support.v4.content.FileProvider { // ContentProvider {

    private Context mContext;

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File cacheDir = getContext().getCacheDir();
        File file = new File(cacheDir, "sono2.wav");
        if (file.exists()) {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }
        throw new FileNotFoundException(uri.getPath());
    }
}
