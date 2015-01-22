package it.uniroma1.lcl.adw.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class CorrelationCalculator 
{
    public static double getSpearman(List<Double> list1, List<Double> list2)
    {
    	SpearmansCorrelation correlation = new SpearmansCorrelation();
    	double c = correlation.correlation(getArray(list1),getArray(list2));
    		
    	return c;
    }
    
    public static double getPearson(List<Double> list1, List<Double> list2)
    {
    	PearsonsCorrelation correlation = new PearsonsCorrelation();
    	double c = correlation.correlation(getArray(list1),getArray(list2));
    		
    	return c;
    }
    
    public static double[] getArray(List<Double> list)
    {
    	double[] array = new double[list.size()];
    	
    	int index = 0;
    	for(double d : list)
    		array[index++] = d;
    	
    	return array;
    	
    }
    
    
    public static void main(String args[])
    {
    	List<Double> list1 = Arrays.asList(0.02,0.04,0.04,0.05,0.06,0.11,0.14,0.18,0.19,
    			0.39,0.42,0.44,0.44,0.45,0.57,0.79,0.85,0.88,0.9,0.91,0.96,0.97,0.97,
    			0.99,1.0,1.09,1.18,1.22,1.24,1.26,1.37,1.41,1.48,1.55,1.69,1.78,1.82,2.37,2.41,2.46,
    			2.61,2.63,2.63,2.69,2.74,3.04,3.11,3.21,3.29,3.41,3.45,3.46,
    			3.46,3.58,3.59,3.6,3.65,3.66,3.68,3.82,3.84,3.88,3.92,3.94,3.94);
    	
    	List<Double> list2 = Arrays.asList(0.467,0.507,0.499,0.563,0.478,0.524,0.582,0.418,0.589,0.559,0.511,0.555,0.531,0.559,0.555,0.568,0.679,0.524,0.598,0.588,0.604,
    			0.650,0.624,0.591,0.541,0.549,0.520,0.618,0.668,0.633,0.570,0.600,0.559,0.558,0.569,0.623,0.639,0.602,0.805,0.604,0.644,0.746,
    			0.758,0.595,0.788,0.980,0.744,1.000,1.000,0.799,0.824,1.000,0.734,0.764,0.926,0.746,1.000,0.764,1.000,0.860,0.722,1.000,1.000,1.000,1.000);

    	System.out.println(getPearson(list1, list2));
    	System.out.println(getSpearman(list1, list2));
    }
    
}
