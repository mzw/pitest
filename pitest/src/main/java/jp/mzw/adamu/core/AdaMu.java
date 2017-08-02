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
	private static final Logger LOGGER = LoggerFactory.getLogger(AdaMu.class);

	private List<String> srcClassList;
	private List<String> testClassList;
	private List<String> mutatorList;
	
	private boolean enabled = true;

	public AdaMu(final File pathToSubjectDir, final MutationOperatorSet type, final boolean enabled) {
		this.srcClassList = listClasses(pathToSubjectDir, "src/main/java");
		this.testClassList = listClasses(pathToSubjectDir, "src/test/java");
		this.mutatorList = new ArrayList<>();
		this.mutatorList.add(type.name());
		this.enabled = enabled;
	}

	public static enum MutationOperatorSet {
		DEFAULTS, STRONGER, ALL
	}

	private List<String> listClasses(File pathToSubjectDir, String pathToTargetDir) {
		final List<String> ret = new ArrayList<>();
		final File root = new File(pathToSubjectDir, pathToTargetDir);
		final List<File> files = listFiles(root);
		for (File file : files) {
			if (file.getName().endsWith(".java") && !file.getName().equals("package-info.java")) {
				String relativePath = root.toURI().relativize(file.toURI()).toString();
				ret.add(relativePath.replaceAll(".java", "").replaceAll("/", "."));
			}
		}
		return ret;
	}

	private List<File> listFiles(File root) {
		final List<File> ret = new ArrayList<>();
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				ret.addAll(listFiles(file));
			} else if (file.isFile()) {
				ret.add(file);
			}
		}
		return ret;
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
		LOGGER.info("Adamu starts to run");

		Log.cleanLatestFiles();
		Knowledge.initDataBases();

		final PluginServices plugins = PluginServices.makeForContextLoader();

		final ReportOptions data = new ReportOptions();
		data.setReportDir("report");
		data.setSourceDirs(new ArrayList<File>());
		data.setGroupConfig(new TestGroupConfig()); // Do not specify any test groups
		data.setVerbose(true);
		data.setAdamuEnabled(this.enabled);

		data.setTargetClasses(Glob.toGlobPredicates(srcClassList));
		data.setTargetTests(Glob.toGlobPredicates(testClassList));
		data.setMutators(mutatorList);

		EntryPoint e = new EntryPoint();
		AnalysisResult result = e.execute(null, data, plugins, new HashMap<String, String>());

		Stats.getInstance().insert(Stats.Label.Finish, System.currentTimeMillis());
		Knowledge.output();
		Log.logPitReport(result);
		Executor._finalize();

		LOGGER.info("Adamu ends to run");
		return result.getStatistics().value();
	}
}
