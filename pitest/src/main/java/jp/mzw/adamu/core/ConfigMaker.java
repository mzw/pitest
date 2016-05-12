package jp.mzw.adamu.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class ConfigMaker {
     static final String DEFAULT_OUTPUT_ROOT_DIR     = "projects";
     
     static final String MAVEN_SOURCE_ROOT_DIR      = "src/main/java";
     static final String MAVEN_TEST_ROOT_DIR      = "src/test/java";

     public static final String SOURCE_CLASS_LIST_FILENAME = "SourceClasses.list";
     public static final String TEST_CLASS_LIST_FILENAME = "TestClasses.list";
     

     public static void makeConfigFiles(String subjectId, String subjectRootDir) throws IOException {
          makeConfigFiles(subjectId, subjectRootDir, DEFAULT_OUTPUT_ROOT_DIR);
     }
     
     public static void makeConfigFiles(String subjectId, String subjectRootDir, String outputRootDir) throws IOException {
          makeConfigFiles(subjectId, subjectRootDir,
                    outputRootDir,
                    MAVEN_SOURCE_ROOT_DIR, MAVEN_TEST_ROOT_DIR,
                    SOURCE_CLASS_LIST_FILENAME, TEST_CLASS_LIST_FILENAME);
     }
     
     public static void makeConfigFiles(String subjectId, String subjectRootDir,
               String outputRootDir,
               String sourceRootDir, String testRootDir,
               String sourceClassListFilename, String testClassListFileName) throws IOException {
          File outputDir = new File(outputRootDir, subjectId);
          
          File srcDir = new File(subjectRootDir, sourceRootDir);
          String sourceClasses = makeClasses(ClassType.SourceClass, srcDir.getAbsolutePath());
          FileUtils.write(new File(outputDir, sourceClassListFilename), sourceClasses);
          
          File testDir = new File(subjectRootDir, testRootDir);
          String testClasses = makeClasses(ClassType.SourceClass, testDir.getAbsolutePath());
          FileUtils.write(new File(outputDir, testClassListFileName), testClasses);
          
     }

     /// To be modified with Visitor pattern 
     enum ClassType {
          SourceClass,
          TestClass
          };
     public static String makeClasses(ClassType classType, String srcRootDir) throws IOException {
          StringBuilder builder = new StringBuilder();
          
          List<File> files = getFilesBy(classType, new File(srcRootDir));
          
          String delim = "";
          for (File file : files) {
               String filepath = file.getAbsolutePath();
               String pathFromSrcRootDir = filepath.substring((srcRootDir + "/").length());
               String clazzWithJavaExtention = pathFromSrcRootDir.replaceAll("/", ".");
               String clazz = clazzWithJavaExtention.substring(0, clazzWithJavaExtention.length() - ".java".length());
               builder.append(delim).append(clazz);
               delim = "\n";
          }
          
          return builder.toString();
     }
     
     private static List<File> getFilesBy(ClassType classType, File fromDir) {
          List<File> ret = new ArrayList<File>();
          
          for (File file : fromDir.listFiles()) {
               if (file.isDirectory()) {
                    ret.addAll(getFilesBy(classType, file));
               } else {
                    String filename = file.getName();
                    switch (classType) {
                    case SourceClass:
                         if (filename.endsWith(".java") && !filename.endsWith("package-info.java")) {
                              ret.add(file);
                         }
                         break;
                    case TestClass:
                         if (filename.endsWith(".java") && !filename.endsWith("package-info.java")) {
                              ret.add(file);
                         }
                         break;
                    default:
                         break;
                    }
               }
          }
          
          return ret;
     }
     
     public static File getClassListFile(String outputDir, String subjectId, ClassType classType) {
          File dir = new File(outputDir, subjectId);
          switch (classType) {
          case SourceClass:
               return new File(dir, SOURCE_CLASS_LIST_FILENAME);
          case TestClass:
               return new File(dir, TEST_CLASS_LIST_FILENAME);
          default:
               return null;
          }
     }
     
}
