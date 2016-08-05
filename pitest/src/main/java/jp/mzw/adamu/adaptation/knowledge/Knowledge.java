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

    public static void initDataBases() {
        for (String className : CLASS_NAMES) {
			try {
	            DataBase db = (DataBase) Class.forName(className).newInstance();
	            db.init();
			} catch (InstantiationException e) {
//				e.printStackTrace();
			} catch (IllegalAccessException e) {
//				e.printStackTrace();
			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
			} catch (SQLException e) {
//				e.printStackTrace();
			}
        }
    }
    
    public static void closeDataBases() {
        for (String className : CLASS_NAMES) {
			try {
				DataBase db = (DataBase) Class.forName(className).newInstance();
	            db.close();
			} catch (InstantiationException e) {
//				e.printStackTrace();
			} catch (IllegalAccessException e) {
//				e.printStackTrace();
			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
			} catch (SQLException e) {
//				e.printStackTrace();
			}
        }
    }
    
    public static void output() {
        for (String className : CLASS_NAMES) {
        	try {
	            KnowledgeBase k = (KnowledgeBase) Class.forName(className).newInstance();
	            k.output();
			} catch (InstantiationException e) {
//				e.printStackTrace();
			} catch (IllegalAccessException e) {
//				e.printStackTrace();
			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
			}
        }
    }
    
}
