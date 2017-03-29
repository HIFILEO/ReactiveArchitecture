package com.example.mvpexample.util;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Helper for reading json data files from src/test/resources.
 */
public class TestResourceFileHelper {
    private static final String RESOURCE_PATH = "/src/test/resources/";

    /**
     * Private constructor to guard against instantiation.
     */
    private TestResourceFileHelper() {
    }

    /**
     * Return the contents of the file with the provided fileName from the test resource directory.
     *
     * @param context - The Context to use when loading the resource.
     * @param fileName - The name of the file in the test resource directory.
     * @return The contents of the file with the provided fileName from the test resource directory.
     * @throws Exception if something goes wrong.
     */
    public static String getFileContentsAsString(Context context, String fileName) throws Exception {
        String filePath = context.getPackageResourcePath() + RESOURCE_PATH + fileName;
        File jsonFile = new File(filePath);
        FileInputStream inputStream = new FileInputStream(jsonFile);
        StringBuilder contents = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            contents.append(line);
        }
        return contents.toString();
    }

    /**
     * Return the contents of the file with the provided fileName from the test resource directory.
     *
     * @param callerClass - test class making the request.
     * @param fileName - The name of the file in the test resource directory.
     * @return The contents of the file with the provided fileName from the test resource directory.
     * @throws Exception if something goes wrong.
     */
    public static String getFileContentAsString(Object callerClass, String fileName) throws Exception {
        ClassLoader classLoader = callerClass.getClass().getClassLoader();
        File jsonFile = new File(classLoader.getResource(fileName).getFile());
        FileInputStream inputStream = new FileInputStream(jsonFile);
        StringBuilder contents = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            contents.append(line);
        }
        return contents.toString();
    }
}
