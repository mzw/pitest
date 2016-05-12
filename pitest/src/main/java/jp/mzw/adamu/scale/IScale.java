package jp.mzw.adamu.scale;

public interface IScale {
     long getObserveTime();
     int getObserveNumMutants();
     double getThresholdStdDev();
     int getAnalyzeInterval();
     int getNoiseFilter();
}
