package jp.mzw.adamu;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.core.AdaMu;
import jp.mzw.adamu.core.ConfigMaker;

/**
 * Command-Line Interface
 * @author Yuta Maezawa
 *
 */
public class CLI {
     public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
          if (args.length == 0) {
               System.err.println("jp.mzw.adamu.CLI <command> <arguments>");
               System.exit(-1);
          }
          String command = args[0];

          if ("run".equals(command)) {
               String[] rargs = Arrays.copyOfRange(args, 1, args.length);
               CLI.run(rargs);
          } else if ("gen".equals(command)) {
               String[] rargs = Arrays.copyOfRange(args, 1, args.length);
               CLI.gen(rargs);
          } else if ("quit".equals(command)) {
               String[] rargs = Arrays.copyOfRange(args, 1, args.length);
               CLI.quit(rargs);
          }
     }
     
     private static void run(String[] args) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
          if (args.length != 1) {
               System.err.println("jp.mzw.adamu.CLI run <subject id>");
               System.exit(-1);
          }
          String subjectId = args[0];
          
          new AdaMu(subjectId).run();
     }
     
     private static void quit(String[] args) throws ClassNotFoundException, SQLException {
          Class.forName("org.sqlite.JDBC");
          Stats.getInstance().insert(Stats.Label.Quit, System.currentTimeMillis());
     }
     
     private static void gen(String[] args) throws IOException {
          if (args.length != 2) {
               System.err.println("jp.mzw.adamu.CLI gen <subject id> <subject root dir>");
               System.exit(-1);
          }
          String subjectId = args[0];
          String subjectRootDir = args[1];
          
          ConfigMaker.makeConfigFiles(subjectId, subjectRootDir);
     }
}
