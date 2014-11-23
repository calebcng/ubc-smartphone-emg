package ceu.marten.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.widget.LinearLayout;
import android.app.Activity;

public class DisplayStoredGraphActivity extends Activity {
    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    
	private final Handler mHandler = new Handler();
//	private ArrayList<Double> dataSet = new ArrayList<Double>();
	public Vector<Double> dataSet = new Vector<Double>();
//	public Vector<Vector<Double>> dataSet = new Vector<Vector<Double>>();
	public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	public String recordingName = "EMG_DATA";
	public String endOfHeader = "# EndOfHeader";
	private GraphView graphView;
	private GraphViewSeries exampleSeries1;
	int n = 0;
	int max=0;
	int min=0;		  
	public static double [] buffer;
	Context context;
	LinearLayout layout;

  
  /*
   * Retrieves data points from specified data file
   * Parameters:	FILE_NAME string, terminated by ".txt"
   * Outputs:	BOOLEAN denoting whether or not the function was successful
   */
  /*private Boolean retrieveDataPoints(String FILE_NAME) {
  	Scanner strings = null;
  	try {
  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, FILE_NAME);
  		
  		if (file.exists()) {
  			FileReader read = new FileReader(file);
  			BufferedReader r = new BufferedReader(read);
  			String line;
  			
  			while((line=r.readLine())!=null) {
  				strings = new Scanner(r);
  	  			
  	      		strings.findWithinHorizon(endOfHeader,0);    		
  	      		strings.useDelimiter("\t *");
  	      		strings.next();
  	      		
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
  			}
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
	System.out.println("Closing strings.");
	  	strings.close();
	  	
	  	return true;
  }*/
 /* private Boolean retrieveDataPoints(String FILE_NAME) {
  	Scanner strings = null;
  	try {
  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, FILE_NAME);
  		
  		if (file.exists()) {
  			FileReader read = new FileReader(file);
  			BufferedReader r = new BufferedReader(read);
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
  }*/
  
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
				
		new ReadFileService().execute();
		
  }
  	
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
	
	private void graphData() {
		
		exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
	        });
				
	  for (int i=0; i<dataSet.size(); i++) {
	  	double pointX = i;
	  	double pointY = dataSet.get(i);
	  	exampleSeries1.appendData(new GraphViewData(pointX, pointY), true, dataSet.size());
	  	System.out.println("X = " + pointX + ", Y = " + pointY);
	  }
	  
	  System.out.println("Defining data set.");
	  GraphView graphView = new LineGraphView(this, recordingName) {
	  	   protected String formatLabel(double value, boolean isValueX) {
	  	      // return as Integer
	  	      return ""+((int) value);
	  	   }
	  	};
	  
	  int yInterval = calculateYScale();
	  int yLabel = max;
	  while ((yLabel-min) % yInterval != 0) {
	  	yLabel++;
	  }
	    
	    // Calculate appropriate interval value in x-direction
	  int xInterval = calculateXScale();	  
	  int xLabel = dataSet.size();
	  while (xLabel % xInterval != 0) {
	  	xLabel++;
	  }
	  
	
	  graphView.addSeries(exampleSeries1); 
	  ((LineGraphView) graphView).setDrawBackground(false);
	  
	  graphView.setScalable(true);  
	  graphView.setScrollable(true);
	  if (dataSet.size() < 50)
	  	graphView.setViewPort(0,dataSet.size());
	  else
	  	graphView.setViewPort(0, 50);
	  graphView.setManualYAxisBounds(yLabel, min);
	  graphView.getGraphViewStyle().setNumVerticalLabels(((yLabel-min)/yInterval) + 1);
	//  graphView.getGraphViewStyle().setNumHorizontalLabels(xLabel/xInterval + 1);
	  graphView.getGraphViewStyle().setGridColor(Color.BLACK);
	  graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
	  graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
	  graphView.getGraphViewStyle().setVerticalLabelsWidth(80);

	  LinearLayout layout = (LinearLayout) findViewById(R.id.dataGraph);
	  layout.addView(graphView);
	}
	
	private int calculateYScale() {
		// Calculate range of y values
		double yBounds[] = {0,0};
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
		return yInterval;
	}
	  
	private int calculateXScale() {
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
		return xInterval;
	}
	
	 // Show Dialog Box with Progress bar
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case progress_bar_type:
        	prgDialog = new ProgressDialog(this);
            prgDialog.setMessage("Reading from file.\nPlease wait...");
            prgDialog.setIndeterminate(true);
            prgDialog.show();
            return prgDialog;
        default:
            return null;
        }
    }
  	
	//<Params, Progress, Result>
	class ReadFileService extends AsyncTask<Void, String, Boolean> {		
		@Override
		protected Boolean doInBackground(Void... args) {		
			/*Scanner strings = null;
			try {
		  		System.out.println(externalStorageDirectory + Constants.APP_DIRECTORY + recordingName + Constants.TEXT_FILE_EXTENTION);
		  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, recordingName + Constants.TEXT_FILE_EXTENTION);
		  		
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
			
			return true;*/
			
		  	Scanner strings = null;
		  	try {
		  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, recordingName + Constants.TEXT_FILE_EXTENTION);
		  		
		  		if (file.exists()) {
		  			FileReader read = new FileReader(file);
		  			BufferedReader r = new BufferedReader(read);
		  			String line;
		  			
		  			while((line=r.readLine())!=null) {
		  				strings = new Scanner(r);
		  	  			
		  	      		strings.findWithinHorizon(endOfHeader,0);    		
		  	      		strings.useDelimiter("\t *");
		  	      		strings.next();
		  	      		
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
		  			}
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
				System.out.println("Closing strings.");
				strings.close();
				return true;
		}
		
		protected void onProgressUpdate(String...progress) {
			//called when the background task makes any progress
//			prgDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@SuppressWarnings("deprecation")
		protected void onPreExecute() {
			//called before doInBackground() is started
			super.onPreExecute();
			// Show Progress Bar Dialog before calling doInBackground method
			showDialog(progress_bar_type);
		}
		
		protected void onPostExecute(Boolean readFileSuccess) {
			//called after doInBackground() has finished 
			if(!readFileSuccess) {
				Random randomGenerator = new Random();			
				System.out.println("@IOERROR: Unable to read from file. Creating random dataset");
				for(int i=0; i<100; i++)
			    {
					dataSet.add(randomGenerator.nextDouble());
			    }
			}
			dismissDialog(progress_bar_type);
			graphData();
		}
	}
}

