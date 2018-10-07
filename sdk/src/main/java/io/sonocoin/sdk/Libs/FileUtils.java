package io.sonocoin.sdk.Libs;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* Created by sciner on 19.10.2017.
*/
public class FileUtils {

    public static void dump(String data) {
        dump(data, true);
    }

    public static void dump(String data, boolean append) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/SonoCoin");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat recordFormat = new SimpleDateFormat("HH-mm-ss");
        File file = new File(dir, "log.txt") ;
        data += "\n";
        data = dateFormat.format(date) + " " + recordFormat.format(date) + ": " + data;
        try {
            final FileOutputStream output = new FileOutputStream(file, append);
            output.write(data.getBytes());
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
