package ceu.marten.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import java.util.Collections;
import java.util.Random;

//import com.example.bluetoothnew.R;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.widget.LinearLayout;
import android.app.Activity;

public class DisplayStoredGraphActivity extends Activity {
	private final Handler mHandler = new Handler();
	private GraphView graphView;
	private GraphViewSeries exampleSeries1;
	private Vector<Double> dataSet = new Vector<Double>();
	private static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	String recordingName = "EMG_DATA";
	String endOfHeader = "# EndOfHeader";
  

  int n = 0;
  int max=0;
  int min=0;
  
  public static double [] buffer;
//  public static double [] buffer = {5, 7, 7, 1, 2, 8, 8, 4, 5, 5, 4};
  
  private double getRandom() {
  	double number = buffer[n];
  	if (number > max){
  		max = (int)number;
  	}
  	else if (number < min){
  		min = (int)number;
  	}
  	n++;
  	return number;
  }
  
  /*
   * Calculates the range (min and max) of values of the dataSet vector
   * Parameters:	none
   * Outputs:	Double[2]; Double[0] = min, Double[1] = max
   */
  private double[] calculateRange() {
  	double dataRange[] = {0, 0}; //{y-min, y-max}
  	Object dataMin = Collections.min(dataSet);
  	Object dataMax = Collections.max(dataSet);
  	dataRange[0] = (double) dataMin;
  	dataRange[1] = (double) dataMax;    	
  	return dataRange;
  }
  
  /*
   * Retrieves data points from specified data file
   * Parameters:	FILE_NAME string, terminated by ".txt"
   * Outputs:	BOOLEAN denoting whether or not the function was successful
   */
  private Boolean retrieveDataPoints(String FILE_NAME) {
  	Scanner strings = null;
  	try {
  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, FILE_NAME);
  		
  		if (file.exists()) {
  			FileReader read = new FileReader(file);
  			strings = new Scanner(read);
  			
      		strings.findWithinHorizon(endOfHeader,0);    		
      		strings.useDelimiter("\t *");
      		strings.next();
  		}
	}
	catch (FileNotFoundException error) {
		System.out.println("@IOERROR: " + error);
		return false;
	}
	catch (IOException error) {
		System.out.println("@IOERROR: " + error);
		return false;
	}
	while (strings.hasNext())
	{
		double dataPoint = Double.parseDouble(strings.next());
		System.out.println("Adding " + dataPoint + " to vector.");
		dataSet.add(dataPoint);
		System.out.println("testData size: " + dataSet.size());
		if (strings.hasNext())
			strings.next();
		else
			break;
	}
	System.out.println("Closing strings.");
	  	strings.close();
	  	
	  	return true;
  }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Initiating creation of DisplayStoredGraphActivity class");
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			recordingName = extras.getString("FILE_NAME");
		}
		else
			System.out.println("Unable to retrieve FILE_NAME");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_stored_graph_activity);
		Random randomGenerator = new Random();
		double yBounds[] = {0,0};		
		
		if (!retrieveDataPoints(recordingName + Constants.TEXT_FILE_EXTENTION))
		{
			System.out.println("@IOERROR: Unable to read from file.");
			for(int i=0; i<100; i++)
		    {
				dataSet.add(randomGenerator.nextDouble());
		    }
		}
		
		exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
	        });
				
	  for (int i=0; i<dataSet.size(); i++) {
	  	double pointX = i;
	  	double pointY = dataSet.get(i);
	  	exampleSeries1.appendData(new GraphViewData(pointX, pointY), true, 1024);
	  	System.out.println("X = " + pointX + ", Y = " + pointY);
	  }
	  
	  System.out.println("Defining data set.");
	  GraphView graphView = new LineGraphView(this, recordingName) {
	  	   protected String formatLabel(double value, boolean isValueX) {
	  	      // return as Integer
	  	      return ""+((int) value);
	  	   }
	  	};
	  
	  // Calculate range of y values
	  yBounds = calculateRange();
	  // Calculate the appropriate max value and min values in y-direction 
	 	max = (int) yBounds[1] + 1;
	    if (yBounds[0] >= 0) {    
	  	min = (int) yBounds[0];
	    }
	    else {
	        min = (int) yBounds[0] - 1;
	    }
	    
	    // Calculate interval level of labels in y-direction
	    int yInterval;
	  if ((max-min) <= 10) {
	  	yInterval = 1;
	  }
	  else if ((max-min) <= 50) {
	  	yInterval = 2;
	  }
	  else if ((max-min) <=100) {
	  	yInterval = 5;
	  }
	  else if ((max-min) <= 200){
	  	yInterval = 10;
	  }
	  else {
	  	yInterval = 25;
	  }
	  int yLabel = max;
	  while ((yLabel-min) % yInterval != 0) {
	  	yLabel++;
	  }
	    
	    // Calculate appropriate interval value in x-direction
	  int xInterval;
	  int xMax = dataSet.size();
	  if (xMax <= 10) {
	  	xInterval = 1;
	  }
	  else if (xMax <= 50) {
	  	xInterval = 5;
	  }
	  else if (xMax <=100) {
	  	xInterval = 10;
	  }
	  else {
	  	xInterval = 20;
	  }
	  int xLabel = xMax;
	  while (xLabel % xInterval != 0) {
	  	xLabel++;
	  }
	  
	
	  graphView.addSeries(exampleSeries1); 
	  ((LineGraphView) graphView).setDrawBackground(false);
//	  ((LineGraphView) graphView).setDrawDataPoints(true);
	  
	  graphView.setScalable(true);  
	  graphView.setScrollable(true);
	  if (dataSet.size() < 50)
	  	graphView.setViewPort(0,dataSet.size());
	  else
	  	graphView.setViewPort(0, 50);
	  graphView.setManualYAxisBounds(yLabel, min);
	  graphView.getGraphViewStyle().setNumVerticalLabels(((yLabel-min)/yInterval) + 1);
//      graphView.getGraphViewStyle().setNumHorizontalLabels(xLabel/xInterval + 1);
      graphView.getGraphViewStyle().setGridColor(Color.BLACK);
      graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
      graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
      graphView.getGraphViewStyle().setVerticalLabelsWidth(80);

      LinearLayout layout = (LinearLayout) findViewById(R.id.dataGraph);
      layout.addView(graphView);
  }
}
