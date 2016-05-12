package jp.mzw.adamu.adaptation.knowledge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.mutationtest.tooling.AnalysisResult;
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
    
    public static void logPitReport(AnalysisResult result) {
        try {
            MutationStatistics stats = result.getStatistics().value().getMutationStatistics();
            PrintStream ps = new PrintStream(new File(Log.getLatestDir(), "pit.report.txt"));

            ps.println(StringUtil.separatorLine('='));
            ps.println("- Statistics");
            ps.println(StringUtil.separatorLine('='));
            stats.report(ps);
            
            ps.println(StringUtil.separatorLine('='));
            ps.println("- Mutators");
            ps.println(StringUtil.separatorLine('='));
            for (Score score : stats.getScores()) {
                score.report(ps);
                ps.println(StringUtil.separatorLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
