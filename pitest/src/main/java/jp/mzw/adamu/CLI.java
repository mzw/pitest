package jp.mzw.adamu;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.core.AdaMu;

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
          } else if ("quit".equals(command)) {
               String[] rargs = Arrays.copyOfRange(args, 1, args.length);
               CLI.quit(rargs);
          }
     }
     
     private static void run(String[] args) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
          if (args.length < 2) {
               System.err.println("jp.mzw.adamu.CLI run <path to subject directory> <mutation operator set> <(optional) enabled>");
               System.exit(-1);
          }
          boolean enabled = args.length == 2 ? true : args[2].equals("disabled") ? false : true;
          AdaMu adamu = new AdaMu(new File(args[0]), AdaMu.MutationOperatorSet.valueOf(args[1].toUpperCase()), enabled);
          adamu.run();
     }
     
     private static void quit(String[] args) throws ClassNotFoundException, SQLException {
          Class.forName("org.sqlite.JDBC");
          Stats.getInstance().insert(Stats.Label.Quit, System.currentTimeMillis());
     }
}
