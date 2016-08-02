package javastat.inference;

/**
 * <p>Title: javastat</p>
 * <p>Description: JAVA programs for statistical computations</p>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p>Company: Tung Hai University</p>
 * @author Wen Hsiang Wei
 * @version 1.4
 */

import java.util.Hashtable;

import JSci.maths.statistics.ChiSqrDistribution;

import javastat.StatisticalAnalysis;
import javastat.StatisticalInference;
import javastat.util.BasicStatistics;
import javastat.util.DataManager;
import javastat.util.Output;

/**
 *
 * <p>Calculates the chi-square statistic and p-value for testing the
 * independence of two variables.</p>
 * <p> </p>
 * <br> Example:
 * <br> double [][] chisqdata1={{29, 22}, {95, 121}, {518, 135}};
 * <br> String [] colvar={"M", "F", "M", "M", "M", "F", "F", "M", "F", "M",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "F", "F", "M", "F", "M", "M", "F", "F", "M", "F",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "M", "F", "F", "F", "F", "F", "M", "F", "M", "F",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "F", "M", "M", "F", "M", "F", "F", "F", "M", "F",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "F", "F", "M", "M", "F", "F", "F", "M", "F", "F"};
 * <br> String [] rowvar={"E", "A", "R", "E", "E", "A", "A", "A", "A", "E",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "E", "A", "A", "A", "R", "R", "A", "A", "A", "E",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "R", "R", "E", "A", "A", "A", "R", "E", "A", "R",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "R", "R", "R", "A", "R", "A", "E", "A", "R", "A",
 * <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *                        "E", "R", "E", "R", "A", "A", "R", "E", "E", "A"};
 * <br>
 * <br> // Non-null constructor
 * <br> ChisqTest testclass1 = new ChisqTest(chisqdata1);
 * <br> double testStatistic = testclass1.testStatistic;
 * <br> double pValue = testclass1.pValue;
 * <br>
 * <br> // Null constructor
 * <br> ChisqTest testclass2 = new ChisqTest();
 * <br> testStatistic = testclass2.testStatistic(colvar, rowvar);
 * <br> pValue = testclass2.pValue(colvar, rowvar);
 * <br>
 * <br> // Non-null constructor
 * <br> Hashtable argument = new Hashtable();
 * <br> StatisticalAnalysis testclass3 =
 * <br> &nbsp;&nbsp;&nbsp;
 *        new ChisqTest(argument, chisqdata1).statisticalAnalysis;
 * <br> testStatistic = (Double) testclass3.output.get(TEST_STATISTIC);
 * <br> pValue = (Double) testclass3.output.get(PVALUE);
 * <br>
 * <br> // Null constructor
 * <br> ChisqTest testclass4 = new ChisqTest(argument, null);
 * <br> testStatistic = testclass4.testStatistic(argument, colvar, rowvar);
 * <br> pValue = testclass4.pValue(argument, colvar, rowvar);
 * <br>
 * <br> // Obtains the information about the output
 * <br> out.println(testclass3.output.toString());
 * <br> out.println(testclass4.output.toString());
 */

public class ChisqTest extends StatisticalInference
{
    /**
     * The chi-square statistic.
     */

    public double testStatistic;

    /**
     * The degree of freedom of the chi-square statistic.
     */

    public double degreeFreedom;

    /**
     * The p value.
     */

    public double pValue;

    /**
     * The input contingency table.
     */

    public double [][] table;

    /**
     * The object represents a chi-square test.
     */

    public StatisticalAnalysis statisticalAnalysis;

    /**
     * The sample size.
     */

    private double sampleSize;

    /**
     * The expected counts in the cell.
     */

    private double ei;

    /**
     * The vector of row means in the contingency table.
     */

    private double [] rowMean;

    /**
     * The vector of column means in the contingency table.
     */

    private double [] columnMean;

    /**
     * The class contains the collections of some basic methods used in
     * statistical computations.
     */

    private BasicStatistics basicStatistics;

    /**
     * Constructs a chi-square test for testing the independence of
     * two variables.
     */

    public ChisqTest(){}

    /**
     * Constructs a chi-square test with the input argument and data.
     * @param argument the empty argument.
     * @param dataObject the input contingency table or column and row
     *                   categorical variables.
     * @exception  IllegalArgumentException wrong input data.
     */

    public ChisqTest(Hashtable argument,
                     Object... dataObject)
    {
        this.argument = argument;
        this.dataObject = dataObject;
        if (dataObject != null &&
            dataObject.length == 2 &&
            dataObject[0].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;") &&
            dataObject[1].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;"))
        {
            statisticalAnalysis = new ChisqTest((String[]) dataObject[0],
                                                (String[]) dataObject[1]);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().equalsIgnoreCase("[[D"))
        {
            statisticalAnalysis = new ChisqTest((double[][]) dataObject);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().
                 equalsIgnoreCase("[Ljava.lang.Object;"))
        {
            statisticalAnalysis = new ChisqTest(
                    DataManager.castDoubleObject(0, dataObject));
        }
        else if (dataObject == null)
        {
            statisticalAnalysis = new ChisqTest();
        }
        else
        {
            throw new IllegalArgumentException("Wrong input data.");
        }
    }

    /**
     * Constructs a chi-square test with the specified contingency table.
     * @param table the input contingency table.
     * @exception  IllegalArgumentException all rows of the input table must
     *                                      have the same length.
     * @exception  IllegalArgumentException all elements of the input table must
     *                                      be postive.
     */

    public ChisqTest(double[]... table)
    {
        this.table = table;
        degreeFreedom = degreeFreedom(table);
        testStatistic = testStatistic(table);
        pValue = pValue(table);
    }

    /**
     * Constructs a chi-square test with the specified row and column
     * categorical variables.
     * @param columnVariable the input column categorical variable.
     * @param rowVariable the input row categorical variable.
     * @exception  IllegalArgumentException all rows of the input table must
     *                                      have the same length.
     * @exception  IllegalArgumentException all elements of the input table must
     *                                      be postive.
     */

    public ChisqTest(String[] columnVariable,
                     String[] rowVariable)
    {
        this(new DataManager().contingencyTable(columnVariable, rowVariable));
    }

    /**
     * The degree of freedom of the chi-square statistic.
     * @param argument the empty argument.
     * @param dataObject the input contingency table or column
     *                   and row categorical variables.
     * @return the degree of freedom.
     */

    public Double degreeFreedom(Hashtable argument,
                                Object... dataObject)
    {
        if (dataObject!=null &&
            dataObject.length==2  &&
            dataObject[0].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;") &&
            dataObject[1].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;"))
        {
            degreeFreedom = degreeFreedom((String[]) dataObject[0],
                                          (String[]) dataObject[1]);
        }
        else if (dataObject!=null &&
                 dataObject.getClass().getName().equalsIgnoreCase("[[D"))
        {
            degreeFreedom = degreeFreedom((double[][]) dataObject);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().
                 equalsIgnoreCase("[Ljava.lang.Object;"))
        {
            degreeFreedom = degreeFreedom(DataManager.
                                          castDoubleObject(0, dataObject));
        }
        else
        {
            throw new IllegalArgumentException("Wrong input data.");
        }

        return degreeFreedom;
    }

    /**
     * The degree of freedom of the chi-square statistic.
     * @param table the input contingency table.
     * @return the degree of freedom.
     */

    public double degreeFreedom(double[]... table)
    {
        this.table = table;
        degreeFreedom = (double) ((table.length - 1) * (table[0].length - 1));
        output.put(javastat.util.Output.DEGREE_OF_FREEDOM, degreeFreedom);

        return degreeFreedom;
    }

    /**
     * The degree of freedom of the chi-square statistic.
     * @param columnVariable the input column categorical variable.
     * @param rowVariable the input row categorical variable.
     * @return the degree of freedom.
     */

    public double degreeFreedom(String[] columnVariable,
                                String[] rowVariable)
    {
        return degreeFreedom(new DataManager().
            contingencyTable(columnVariable, rowVariable));
    }

    /**
     * The chi-square statistic.
     * @param argument the empty argument.
     * @param dataObject the input contingency table or column and row
     *                   categorical variables.
     * @return the value of the chi-square statistic.
     * @exception IllegalArgumentException wrong input data.
     * @exception IllegalArgumentException all rows of the input table must
     *                                     have the same length.
     * @exception IllegalArgumentException all elements of the input table must
     *                                     be postive.
     */

    public Double testStatistic(Hashtable argument,
                                Object... dataObject)
    {
        this.argument = argument;
        this.dataObject = dataObject;
        if (dataObject != null &&
            dataObject.length==2 &&
            dataObject[0].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;") &&
            dataObject[1].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;"))
        {
            testStatistic = testStatistic((String[]) dataObject[0],
                                          (String[]) dataObject[1]);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().equalsIgnoreCase("[[D"))
        {
            testStatistic = testStatistic((double[][]) dataObject);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().
                 equalsIgnoreCase("[Ljava.lang.Object;"))
        {
            testStatistic = testStatistic(DataManager.
                                          castDoubleObject(0, dataObject));
        }
        else
        {
            throw new IllegalArgumentException("Wrong input data.");
        }

        return testStatistic;
    }

    /**
     * The chi-square statistic.
     * @param table the input contingency table.
     * @return the value of the chi-square statistic.
     * @exception IllegalArgumentException all rows of the input table must
     *                                     have the same length.
     * @exception IllegalArgumentException all elements of the input table must
     *                                     be postive.
     */

    public double testStatistic(double[]... table)
    {
        this.table = table;
        new DataManager().checkPositiveRangeDimension(table);
        testStatistic = 0.0;
        basicStatistics = new BasicStatistics();
        rowMean = basicStatistics.meanVector(table);
        columnMean = basicStatistics.columnMeanVector(table);
        sampleSize = basicStatistics.sum(table);
        for (int i = 0; i < table[0].length; i++)
        {
            for (int k = 0; k < table.length; k++)
            {
                ei = (table.length * table[0].length *
                    rowMean[k] * columnMean[i]) / sampleSize;
                testStatistic += Math.pow(table[k][i] - ei, 2.0) / ei;
            }
        }
        output.put(Output.TEST_STATISTIC, testStatistic);

        return testStatistic;
    }

    /**
     * The chi-square statistic.
     * @param columnVariable the input column categorical variable.
     * @param rowVariable the input row categorical variable.
     * @return the value of the chi-square statistic.
     * @exception IllegalArgumentException all rows of the input table must
     *                                     have the same length.
     * @exception IllegalArgumentException all elements of the input table must
     *                                     be postive.
     */

    public double testStatistic(String[] columnVariable,
                                String[] rowVariable)
    {
        return testStatistic(new DataManager().
            contingencyTable(columnVariable, rowVariable));
    }

    /**
     * The p value.
     * @param argument the empty argument.
     * @param dataObject the input contingency table or column
     *                   and row categorical variables.
     * @return the p value for the test.
     * @exception  IllegalArgumentException wrong input data.
     * @exception  IllegalArgumentException all rows of the input table must
     *                                      have the same length.
     * @exception  IllegalArgumentException all elements of the input table must
     *                                      be postive.
     */

    public Double pValue(Hashtable argument,
                         Object... dataObject)
    {
        this.argument = argument;
        this.dataObject = dataObject;
        if (dataObject != null &&
            dataObject.length == 2 &&
            dataObject[0].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;") &&
            dataObject[1].getClass().getName().
            equalsIgnoreCase("[Ljava.lang.String;"))
        {
            pValue = pValue((String[]) dataObject[0], (String[]) dataObject[1]);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().equalsIgnoreCase("[[D"))
        {
            pValue = pValue((double[][]) dataObject);
        }
        else if (dataObject != null &&
                 dataObject.getClass().getName().
                 equalsIgnoreCase("[Ljava.lang.Object;"))
        {
            pValue = pValue(DataManager.castDoubleObject(0, dataObject));
        }
        else
        {
            throw new IllegalArgumentException("Wrong input data.");
        }

        return pValue;
    }

    /**
     * The p value.
     * @param table the input contingency table.
     * @return the p value for the test.
     * @exception IllegalArgumentException all rows of the input table must
     *                                     have the same length.
     * @exception IllegalArgumentException all elements of the input table must
     *                                     be postive.
     */

    public double pValue(double[]... table)
    {
        this.table = table;
        testStatistic = testStatistic(table);
        pValue = 1 - new ChiSqrDistribution((double) ((table.length - 1) *
            (table[0].length - 1))).cumulative(testStatistic);
        output.put(Output.PVALUE, pValue);

        return pValue;
    }

    /**
     * The p value.
     * @param columnVariable the input column categorical variable.
     * @param rowVariable the input row categorical variable.
     * @return the p value for the test.
     * @exception IllegalArgumentException all rows of the input table must
     *                                     have the same length.
     * @exception IllegalArgumentException all elements of the input table must
     *                                     be postive.
     */

    public double pValue(String[] columnVariable,
                         String[] rowVariable)
    {
        return pValue(new DataManager().
            contingencyTable(columnVariable, rowVariable));
    }

}
