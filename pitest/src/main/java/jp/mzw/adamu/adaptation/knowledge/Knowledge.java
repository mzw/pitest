package jp.mzw.adamu.adaptation.knowledge;

import java.sql.SQLException;

/**
 * Knowledge of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Knowledge {

    public static final String[] CLASS_NAMES = new String[] {
        Stats.class.getName(),
        Mutations.class.getName(),
        TestResults.class.getName(),
        RtMS.class.getName(),
        AMS.class.getName(),
        Overhead.class.getName(),
    };

    public static void initDataBases() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        for (String className : CLASS_NAMES) {
            DataBase db = (DataBase) Class.forName(className).newInstance();
            db.init();
        }
    }
    
    public static void closeDataBases() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (String className : CLASS_NAMES) {
            DataBase db = (DataBase) Class.forName(className).newInstance();
            db.close();
        }
    }
    
    public static void output() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (String className : CLASS_NAMES) {
            KnowledgeBase k = (KnowledgeBase) Class.forName(className).newInstance();
            k.output();
        }
    }
    
}
