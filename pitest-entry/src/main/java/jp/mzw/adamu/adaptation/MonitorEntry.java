package jp.mzw.adamu.adaptation;

import jp.mzw.adamu.adaptation.knowledge.Mutations;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.build.WorkerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonitorEntry {

    /**
     * Order mutants to be examined by test cases
     * according to methods where PIT applies mutation operators to create them
     * @param codeClasses
     * @param mutations
     * @param grouper
     * @param workerFactory
     * @return
     */
    public static List<MutationAnalysisUnit> orderTestExecutionOnMutants(final Collection<ClassName> codeClasses, final Collection<MutationDetails> mutations,
                                                                         final MutationGrouper grouper, WorkerFactory workerFactory, final boolean enableAdamu) {
        long start = System.currentTimeMillis();
        List<MutationAnalysisUnit> ret = new ArrayList<MutationAnalysisUnit>();

        Map<String, List<MutationDetails>> methodMutationMap = new HashMap<>();
        for (MutationDetails mutation : mutations) {
            String methodName = mutation.getClassName() + "#" + mutation.getMethod();
            List<MutationDetails> mutationList = methodMutationMap.get(methodName);
            if (mutationList == null) {
                mutationList = new ArrayList<>();
            }
            mutationList.add(mutation);
            methodMutationMap.put(methodName, mutationList);
        }
        for (String methodName : methodMutationMap.keySet()) {
            List<MutationDetails> mutationList = methodMutationMap.get(methodName);
            Collections.shuffle(mutationList);
        }
        boolean remain = true;
        do {
            remain = false;
            Collection<MutationDetails> methodBasedMutationList = new ArrayList<>();
            for (String methodName : methodMutationMap.keySet()) {
                List<MutationDetails> mutationList = methodMutationMap.get(methodName);
                if (0 < mutationList.size()) {
                    MutationDetails mutation = mutationList.remove(0);
                    methodBasedMutationList.add(mutation);
                }
                if (!mutationList.isEmpty()) {
                    remain = true;
                }
            }
            final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
            FCollection.flatMapTo(methodBasedMutationList, MutationTestBuilder.mutationDetailsToTestClass(), uniqueTestClasses);
            MutationTestUnit mtu = new MutationTestUnit(methodBasedMutationList, uniqueTestClasses, workerFactory, enableAdamu);
            ret.add(mtu);
        } while (remain);

        long end = System.currentTimeMillis();
        Overhead.getInstance().insert(Overhead.Type.TestExecOrder, end - start);

        // Store mutation information
        try {
            Mutations db = Mutations.getInstance();
            for (MutationAnalysisUnit unit : ret) {
                MutationTestUnit unitCast = (MutationTestUnit) unit;
                for (MutationDetails mutation : unitCast.getAvailableMutations()) {
                    db.insert(mutation.hashCode(), mutation.getClassName().toString(), mutation.getMethod().name(), mutation.getLineNumber(),
                            mutation.getMutator());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
