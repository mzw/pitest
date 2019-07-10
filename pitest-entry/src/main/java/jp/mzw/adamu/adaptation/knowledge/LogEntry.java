package jp.mzw.adamu.adaptation.knowledge;

import org.pitest.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.mutationtest.tooling.AnalysisResult;

public class LogEntry {
    public static void logPitReport(AnalysisResult result) {
        try {
            MutationStatistics stats = result.getStatistics().get().getMutationStatistics();
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
