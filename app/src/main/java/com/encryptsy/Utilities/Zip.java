package com.encryptsy.Utilities;

/**
 * Created by Chris on 7/22/2015.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    /**
     * Zip it
     *
     * @param output output ZIP file location
     */
    public static List<String> zipDirectory(String source, String output) {

        List<String> fileList = generateFileList(source, new File(source));

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(output);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String file : fileList) {

                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(source + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            return fileList;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return  null;
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    public static ArrayList<String> generateFileList(String source, File node) {

        ArrayList<String> fileList = new ArrayList<String>();

        //add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(source, node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(source, new File(node, filename));
            }
        }

        return fileList;
    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private static String generateZipEntry(String source, String file) {
        return file.substring(source.length() + 1, file.length());
    }
}