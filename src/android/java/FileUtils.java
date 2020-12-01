package com.cordovapluginfastcam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class FileUtils {
    /**
     * Recursively deletes
     * the files in a folder,
     * if older then olderThen
     * @param fileOrDirectory
     * @param olderThen
     */
    public static void deleteRecursive(File fileOrDirectory, long olderThen) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child, olderThen);
            }
        } else {
            long lastModified = fileOrDirectory.lastModified();
            long fileAge = System.currentTimeMillis() - lastModified;
            if (fileAge >= olderThen) {
                fileOrDirectory.delete();
            }
        }
    }

    /**
     * Reads a number matrix from
     * a text file in the form of:
     *
     * n n n n
     * n n n n
     * n n n n
     * .......
     */
    public static double[][] readMatrix(InputStream file) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        ArrayList<double[]> rows = new ArrayList<>();

        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] splitted = line.split(" ");
                double[] parsed = new double[splitted.length];
                for (int j = 0; j < splitted.length; j += 1) {
                    parsed[j] = Double.parseDouble(splitted[j]);
                }
                rows.add(parsed);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        int numRows = rows.size();
        int numCols = rows.get(0).length;
        double[][] matrix = new double[numRows][numCols];

        for (int currRow = 0; currRow < rows.size(); currRow += 1) {
            matrix[currRow] = rows.get(currRow);
        }

        return matrix;
    }
}
