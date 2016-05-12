package jp.mzw.adamu.scale;

public abstract class Scale implements IScale {

     protected final long time;
     protected final int num;
     protected final double stddev;
     protected final int interval;
     protected final int noise;

     public Scale(long time, int num, double stddev,
               int interval, int noise) {
          this.time = time;
          this.num = num;
          this.stddev = stddev;
          this.interval = interval;
          this.noise = noise;
     }

     public long getObserveTime() {
          return this.time;
     }

     public int getObserveNumMutants() {
          return this.num;
     }

     public double getThresholdStdDev() {
          return this.stddev;
     }

     public int getAnalyzeInterval() {
          return this.interval;
     }

     public int getNoiseFilter() {
          return this.noise;
     }

     public static Scale getScale(int mutants, int tests) {
          int scale = mutants * tests;
          if (scale < 100000) { // less than 0.1M
               return new Small();
          } else if (scale < 1000000) { // less than 1M
               return new Medium();
          } else if (scale < 10000000) { // less than 10M
               return new Large();
          } else {
               return new ExtraLarge();
          }
     }
}
