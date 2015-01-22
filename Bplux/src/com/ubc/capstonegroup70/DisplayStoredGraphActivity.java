package com.ubc.capstonegroup70;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.Collections;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.lang.String;

import com.bitalino.util.SensorDataConverter;

//import com.example.bluetoothnew.R;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.app.Activity;

/** 
 * Reads the data stored in a target recordings text file and plots
 * the data onto a graph.
 * 
 * @author Caleb Ng
 */

public class DisplayStoredGraphActivity extends Activity {
    // Progress Dialog Object
    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    
	private final Handler mHandler = new Handler();
	public Vector<Double> dataSet = new Vector<Double>();
	public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	public String recordingName = "EMG_DATA";
	public String endOfHeader = "# EndOfHeader";
	private GraphViewSeries exampleSeries1;
	int setSize = 0;
	int max=0;
	int min=0;	
	public static double [] buffer;
	Context context;
	LinearLayout layout;
	// Data for determining the appropriate scale for the x-axis
	private int samplingFrequency = 1;
	// Data for determining the appropriate time stamp
	private String startDate = "Jan 1, 2000";
	private int startHour = 00;
	private int startMinute = 00;
	private int startSecond = 00;
	private String endDate = "Jan 1, 2000";
	private int endHour = 00;
	private int endMinute = 00;
	private int endSecond = 00;
	private String PeridOfDay = "AM";
	private int sampleLength = 0;

  
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
		prgDialog = new ProgressDialog(this);
				
		new ReadFileService().execute();
		
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
	  System.out.println("Defining data set.");
	  GraphView graphView = new LineGraphView(this, recordingName) {
		  protected String formatLabel(double value, boolean isValueX) {
			  if (isValueX) {
//				return String.format("%d", (int) value);
				  long xValue;
				  if (value < 0.000){
					  xValue = 0;
					  return "00:00:00";
				  }
				  xValue = (long) value;
				  return String.format("%02d:%02d:%02d",(int) (startHour + (xValue / (samplingFrequency*60*60)) % 24), (int) (startMinute + (xValue / (samplingFrequency*60)) % 60), (int) (startSecond + (xValue / samplingFrequency)) % 60);
			  } else {
				return String.format("%.2f", (double) value);
	//			return null;
			}
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
	  if (dataSet.size() < 100)
	  	graphView.setViewPort(0,dataSet.size());
	  else
	  	graphView.setViewPort(0, 100);
	  graphView.setManualYAxisBounds(yLabel, min);
//	  graphView.getGraphViewStyle().setNumVerticalLabels(((yLabel-min)/yInterval) + 1);
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
//		if ((max-min) <= 5) {
//			yInterval = 0.5;
//		}
//		else 
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
	
	/**
	 * Destroys activity
	 */
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
  	
	//<Params, Progress, Result>
	class ReadFileService extends AsyncTask<Void, String, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... args) {		
			Scanner strings = null;
			InputStream stream = null;
			ZipInputStream zipInput = null;
			try {
				System.out.println(externalStorageDirectory + Constants.APP_DIRECTORY + recordingName + Constants.ZIP_FILE_EXTENTION);
		  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, recordingName);
		  		ZipFile zipFile = new ZipFile(file);
		  		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		  				  		
		  		while (entries.hasMoreElements()) {
		  			ZipEntry zipEntry = entries.nextElement();
		  			stream = zipFile.getInputStream(zipEntry);
		  			strings = new Scanner(stream);
		  			/*
		  			  // Process non-compressed text files
		  			  if (!zipEntry.isDirectory()) {
		  				String fileName = zipEntry.getName();
		  				if (fileName.endsWith(".txt")) {
		  					zipInput = new ZipInputStream(new FileInputStream(fileName));
		  				}
		  			}*/
		  			
		  			// Extract a substring of the file header text
//		  			System.out.println("Extracting header substring.");
		  			String regexPattern = endOfHeader; //"\"ColumnLabels\"";
		  			strings.useDelimiter(regexPattern);
		  			String extracted = strings.next();
		  			System.out.println("Extracted: " + extracted);
		  			
		  			// Determine the sampling frequency from the header text
		  			System.out.println("Extracting sampling frequency.");
		  			Pattern pattern = Pattern.compile("\"SamplingFrequency\": \"(\\d+)\"");
		  			Matcher matcher = pattern.matcher(extracted);
		  			if (matcher.find()) {
		  				samplingFrequency = Integer.parseInt(matcher.group(1));
		  				System.out.println(samplingFrequency);
		  			}
		  			
		  			// Determine the start date and time from the header text
		  			System.out.println("Extracting start date and time.");
		  			pattern = Pattern.compile("StartDateTime\": \"(\\w+\\s\\d+,\\s\\d+) (\\d+):(\\d+):(\\d+) (\\w+)\"");
		  			matcher = pattern.matcher(extracted);
		  			if (matcher.find()) {
		  				if (matcher.groupCount() == 5) {
			  				startDate = matcher.group(1);
			  				startHour = Integer.parseInt(matcher.group(2));
			  				startMinute = Integer.parseInt(matcher.group(3));
			  				startSecond = Integer.parseInt(matcher.group(4));
			  				PeridOfDay = matcher.group(5);
			  				if (PeridOfDay == "PM")
			  					endHour += 12;
			  				System.out.println("Extracted end time to be: " + startHour + ":" + startMinute + ":" + startSecond + " on " + startDate);
		  				}
		  				else
		  					System.out.print("ERROR: Insufficient number of matches found: " + matcher.groupCount());
		  			}
		  			
		  			// Use tabs as a delimiter for file data
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
			// Loops for as long as there are more data points to be read from the text file
			while (strings.hasNext())
			{
				double dataPoint = Double.parseDouble(strings.next());
				dataSet.add(SensorDataConverter.scaleEMG(dataPoint));
				if (strings.hasNext())
					strings.next();
				else
					break;
			}
			System.out.println("Closing strings.");
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	strings.close();			
			return true;
		}
		
		protected void onProgressUpdate(String...progress) {
			//called when the background task makes any progress
		}

		protected void onPreExecute() {
			//called before doInBackground() is started
			super.onPreExecute();
			// Show Progress Bar Dialog before calling doInBackground method
//			showDialog(progress_bar_type);
			prgDialog.setTitle("Opening File");
			prgDialog.setMessage("Opening " + recordingName + "\nPlease wait...");
			prgDialog.show();
		}
		
		protected void onPostExecute(Boolean readFileSuccess) {
			//called after doInBackground() has finished 
			// Check if the file was read successfully. If not, output error message and generate sample set of data
			if(!readFileSuccess) {
				Random randomGenerator = new Random();			
				System.out.println("@IOERROR: Unable to read from file. Creating random dataset");
				for(int i=0; i<100; i++)
			    {
					dataSet.add(randomGenerator.nextDouble());
			    }
			}

			// Prepare data set for graphing
			exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
	        });
			for (int i=0; i<dataSet.size(); i++) {
			  	double pointX = i;
			  	double pointY = dataSet.get(i);
			  	exampleSeries1.appendData(new GraphViewData(pointX, pointY), true, dataSet.size());
//			  	System.out.println("X = " + pointX + ", Y = " + pointY);
			}
			graphData();
//			dismissDialog(progress_bar_type);
			prgDialog.dismiss();
			prgDialog = null;
		}
	}
}

