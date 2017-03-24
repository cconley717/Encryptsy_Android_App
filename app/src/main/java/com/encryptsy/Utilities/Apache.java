package com.encryptsy.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Chris on 7/15/2015.
 */
public class Apache {

    private void copyFile(String source, String destination) throws IOException {

        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        if (!sourceFile.exists()) {
            return;
        }

        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        FileChannel destinationChannel = new FileOutputStream(destinationFile).getChannel();

        if (destinationChannel != null && sourceChannel != null) {
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        if (sourceChannel != null) {
            sourceChannel.close();
        }
        if (destinationChannel != null) {
            destinationChannel.close();
        }
    }
    /*
    public static void copyFile(String source, String destination)
    {

        try {
            FileUtils.copyFile(new File(source), new File(destination));
        } catch (IOException e) {
            Log.d("testing", "ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
}
