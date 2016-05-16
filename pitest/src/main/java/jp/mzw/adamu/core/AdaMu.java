package jp.mzw.adamu.core;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.mzw.adamu.adaptation.Executor;
import jp.mzw.adamu.adaptation.knowledge.Knowledge;
import jp.mzw.adamu.adaptation.knowledge.Log;
import jp.mzw.adamu.adaptation.knowledge.Stats;

import org.apache.commons.io.FileUtils;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.tooling.AnalysisResult;
import org.pitest.mutationtest.tooling.CombinedStatistics;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaMu {
    static Logger logger = LoggerFactory.getLogger(AdaMu.class);

    String subjectId;
    List<String> srcClassList;
    List<String> testClassList;
    List<String> mutatorList;
     
    /**
     * 
     * @param subjectId
     * @param subjectRootDir
     * @throws IOException
     */
    public AdaMu(String subjectId) throws IOException {
        this.subjectId = subjectId;
        
        loadSourceTestList("res/projects", subjectId);
        loadMutatorList("res/mutators/", "StrongerMutators.list");
    }
     
    public String getSubjectId() {
        return this.subjectId;
    }

    public List<String> getSourceClassList() {
        return this.srcClassList;
    }
    
    public List<String> getTestClassList() {
        return this.testClassList;
    }
     
    public List<String> getMutatorList() {
        return this.mutatorList;
    }
     
    public void loadSourceTestList(String dir, String subjectId) throws IOException {
        this.srcClassList = FileUtils.readLines(
                  ConfigMaker.getClassListFile(dir, subjectId, ConfigMaker.ClassType.SourceClass));
        this.testClassList = FileUtils.readLines(
                  ConfigMaker.getClassListFile(dir, subjectId, ConfigMaker.ClassType.TestClass));
    }
    
    public void loadMutatorList(String dir, String filename) throws IOException {
        this.mutatorList = FileUtils.readLines(new File(dir, filename));
    }
     
    /**
     * Runs PIT
     * @return Results of mutation testing
     * @throws IOException 
     * @throws SQLException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    public CombinedStatistics run() throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.info("Adamu starts to run");
       
        Log.cleanLatestFiles();
        Knowledge.initDataBases();
       
        final PluginServices plugins = PluginServices.makeForContextLoader();
        
        final ReportOptions data = new ReportOptions();
        data.setReportDir("report");
        data.setSourceDirs(new ArrayList<File>());
        data.setGroupConfig(new TestGroupConfig()); // Do not specify any test groups
        data.setVerbose(true);
        
        data.setTargetClasses(Glob.toGlobPredicates(srcClassList));
        data.setTargetTests(Glob.toGlobPredicates(testClassList));
        data.setMutators(mutatorList);
        
        Stats.getInstance().insertNumTests(testClassList.size());
        
        EntryPoint e = new EntryPoint();
        AnalysisResult result = e.execute(null, data, plugins, new HashMap<String, String>());
        
        Stats.getInstance().insert(Stats.Label.Finish, System.currentTimeMillis());
        Knowledge.output();
        Log.logPitReport(result);
        Executor.execute();
        
        // Send mail as notification
        Runtime.getRuntime().exec(new String[]{"/bin/sh", "mail", getSubjectId()}, null, new File("sh"));
        
        return result.getStatistics().value();
    }
}
