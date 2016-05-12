package jp.mzw.adamu.core;

import java.io.File;

public class PITModifier {
     public static String getClassPathStartsWithJunit() {
          StringBuilder builder = new StringBuilder();
          builder.append("lib/junit-4.12.jar");
          builder.append(File.pathSeparator);
          builder.append(System.getProperty("java.class.path"));
          return builder.toString();
     }
}
