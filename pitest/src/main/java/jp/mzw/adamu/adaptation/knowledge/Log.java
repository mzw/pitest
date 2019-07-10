package jp.mzw.adamu.adaptation.knowledge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import org.pitest.util.StringUtil;

public class Log {

    public static final String DIR = "logs";
    public static final File LATEST_DIR = getLatestDir();
    
    public static File getLatestDir() {
        File dir = new File(DIR, "latest");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    private static String getTimestamp() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return sdf.format(c.getTime());
    }
    
    public static void copyResultsWithTimestamp() {
        try {
            File dst = new File(DIR, getTimestamp());
            if (!dst.exists()) {
                dst.mkdir();
            }
            FileUtils.copyDirectory(LATEST_DIR, dst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void cleanLatestFiles() {
        try {
            for (File file : LATEST_DIR.listFiles()) {
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
