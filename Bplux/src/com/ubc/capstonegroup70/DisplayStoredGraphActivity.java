package com.ubc.capstonegroup70;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.lang.String;

import com.bitalino.util.SensorDataConverter;

//import com.example.bluetoothnew.R;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.model.io.DataManager;
import ceu.marten.ui.NewRecordingActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import org.apache.commons.math3.complex.Complex;
import org.jtransforms.fft.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

/** 
 * Reads the data stored in a target recordings text file and plots
 * the data onto a graph.
 * 
 * @author Caleb Ng
 */

public class DisplayStoredGraphActivity extends Activity {
    // Tag for logging errors
	private static final String DSGA_TAG = DisplayStoredGraphActivity.class.getName();
	// Progress Dialog Object
    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    
	private final Handler mHandler = new Handler();
	private Vector<Double> dataSetRAW = new Vector<Double>();
	private Vector<Double> dataSetFFT = new Vector<Double>();
	private Vector<Double> dataSetPWR = new Vector<Double>();

	public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	public String recordingName = "EMG_DATA";
	public String recordingTimePattern = ".{168}(\\d+).(\\d+).(\\d\\d)(\\d+).(\\d+).(\\d\\d)";
	private String graphTitle = new String();
	private static final String TAG = NewRecordingActivity.class.getName();
	private GraphViewSeries rawSeries;
	private GraphViewSeries fftSeries;
	private GraphViewSeries pwrSeries;
	int setSize = 0;
	int max=0;
	int min=0;	
	public static double [] buffer;
	Context context;
	LinearLayout layout;
	// Data for determining the appropriate scale for the x-axis
	private int samplingFrequency = 1000;
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
	private String recordingDate;
	private String recordingTime;

  
  	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Initiating creation of DisplayStoredGraphActivity class");
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			recordingName = extras.getString("FILE_NAME");
			graphTitle = "Recording for " + extras.getString("PATIENT_NAME");
		}
		else
			System.out.println("Unable to retrieve FILE_NAME");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_stored_graph_activity);
		prgDialog = new ProgressDialog(this);
				
		new ReadFileService().execute();
		
  }
  	
  	/*
  	 * Responds to changes in the radio button selection
  	 * Choice of radio button selection will determine which data set to plot:
  	 * 		1) dataSetRAW - the raw, unprocessed EMG signal
  	 * 		2) dataSetFFT_real - the EMG signal after FFT has been performed on it
  	 */
  	public void onRadioButtonClicked(View view) {
  		// Is the button checked?
  		boolean checked = ((RadioButton) view).isChecked();
  		final RadioButton rawData = (RadioButton) findViewById(R.id.rawGraphBtn);
  		final RadioButton fftData = (RadioButton) findViewById(R.id.fftGraphBtn);
  		final RadioButton pwrData = (RadioButton) findViewById(R.id.pwrGraphBtn);
  		
  		// Check which button was clicked
  		switch(view.getId()) {
	  		case R.id.rawGraphBtn:
	  			if (checked) {
	  				// Original signal option selected
	  				rawData.setClickable(false);
	  				fftData.setClickable(true);
	  				pwrData.setClickable(true);
	  				
	  				graphData(dataSetRAW,100);
	  			}
	  			break;
	  		case R.id.fftGraphBtn:
	  			if (checked) { 
	  				// Frequency spectrum option selected
	  				rawData.setClickable(true);
	  				fftData.setClickable(false);
	  				pwrData.setClickable(true);
	  				
	  				//If FFT has not already been calculated, do so now
	  				if(dataSetFFT.size() == 0 || dataSetPWR.size() == 0) {
	  					// Perform FFT to compute graph series
	  					prgDialog = new ProgressDialog(this);		
	  					new CalculateFFT().execute(true);
	  				}
	  				else
	  					graphData(dataSetFFT,dataSetFFT.size());
	  			}
	  			break;
	  		case R.id.pwrGraphBtn:
	  			if (checked) {
	  				// Power spectrum option selected
	  				rawData.setClickable(true);
	  				fftData.setClickable(true);
	  				pwrData.setClickable(false);
	  				
	  				//If FFT has not already been calculated, do so now
	  				if(dataSetFFT.size() == 0 || dataSetPWR.size() == 0) {
	  					// Perform FFT to compute graph series
	  					prgDialog = new ProgressDialog(this);		
	  					new CalculateFFT().execute(false);
	  				}
	  				else
	  					graphData(dataSetPWR,dataSetPWR.size());
	  			}
	  			break;
  		}
  	}
  

	  /*
	   * Calculates the range (min and max) of values of the dataSet vector
	   * Parameters:	none
	   * Outputs:	Double[2]; Double[0] = min, Double[1] = max
	   */
	private double[] calculateRange(Vector<Double> dataSet) {
		double dataRange[] = {0, 0}; //{y-min, y-max}
		Object dataMin = Collections.min(dataSet);
		Object dataMax = Collections.max(dataSet);
		dataRange[0] = (double) dataMin;
		dataRange[1] = (double) dataMax;    	
		return dataRange;
	}
	
	private void graphData(final Vector<Double> dataSet, int viewPort) {	  
	  System.out.println("Defining data set.");
	  
	  // Determine the appropriate graphSeries to add depending on dataSet that was passed
	  GraphViewSeries graphSeries;
	  TextView yAxisString = (TextView) findViewById(R.id.graph_yAxis);
	  TextView xAxisString = (TextView) findViewById(R.id.graph_xAxis);
	  if( dataSet == dataSetFFT ) {
		  graphSeries = fftSeries;
		  yAxisString.setText("Voltage\n(mV)");
		  xAxisString.setText("Frequency (Hz)");
	  }
	 else if (dataSet == dataSetPWR ) {
		 graphSeries = pwrSeries;
		 yAxisString.setText("Power  \n(dB/Hz)");
		 xAxisString.setText("Frequency (Hz)");
	 }
	 else {
		  graphSeries = rawSeries;
		  yAxisString.setText("Voltage\n(mV)");
		  xAxisString.setText("Time (Hour:Minute:Second)");
	  }
	  // Format graph labels to show the appropriate domain on x-axis
	  GraphView graphView = new LineGraphView(this, graphTitle) {
		  protected String formatLabel(double value, boolean isValueX) {
			  if (isValueX) {
				  long xValue;
				  if (value < 0.000){
					  xValue = 0;
					  return "00:00:00";
				  }
				  xValue = (long) value;
				  if(dataSet == dataSetFFT || dataSet == dataSetPWR) {
					  // Set x-axis to use the frequency domain
					  return String.format("%d",(int) (xValue * samplingFrequency /dataSetRAW.size()));  
				  }
				  else {
					  // Set the x-axis to use the time domain
					  return String.format("%02d:%02d:%02d",(int) ((xValue / (samplingFrequency*60*60)) % 24), (int) ((xValue / (samplingFrequency*60)) % 60), (int) ((xValue / samplingFrequency)) % 60);
				  }						  
					  
			  } else {
				  if(dataSet == dataSetFFT || dataSet == dataSetPWR) {
					  return String.format("%d", (int) value);
				  }
				  else
					  return String.format("%.2f", (double) value);
			}
		  }
	  };
	  
	  int yInterval = calculateYScale(dataSet);
	  int yLabel = max;
	  while ((yLabel-min) % yInterval != 0) {
	  	yLabel++;
	  }
	    
	    // Calculate appropriate interval value in x-direction
	  int xInterval = calculateXScale(dataSet);	  
	  int xLabel = dataSet.size();
	  while (xLabel % xInterval != 0) {
	  	xLabel++;
	  }
	  
	  graphView.addSeries(graphSeries);
	  ((LineGraphView) graphView).setDrawBackground(false);
	  
	  // Settings for the graph to be scrollable and scalable
	  graphView.setScalable(true);  
	  graphView.setScrollable(true);
	  // Settings for graph view port size
	  if (dataSet.size() < viewPort)
	  	graphView.setViewPort(0,dataSet.size());
	  else
	  	graphView.setViewPort(0, viewPort);
	  // Settings for the graph styling
	  graphView.setManualYAxisBounds(yLabel, min);
	  graphView.getGraphViewStyle().setGridColor(Color.BLACK);
	  graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
	  graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
	  graphView.getGraphViewStyle().setVerticalLabelsWidth(80);

	  LinearLayout layout = (LinearLayout) findViewById(R.id.dataGraph);
	  layout.removeAllViews();
	  layout.addView(graphView);
	}
	
	private int calculateYScale(Vector<Double> dataSet) {
		// Calculate range of y values
		double yBounds[] = {0,0};
		yBounds = calculateRange(dataSet);
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
	  
	private int calculateXScale(Vector<Double> dataSet) {
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
	
	/*
	 * Calculates the mean value (average) of a given dataSet vector and subtracts the mean from the original dataset
	 * Parameters:		Vector<Double> - Vector of type double containing the values from which the mean will be calculated
	 * Outputs:			Vector<Double> - Value of the original vector with the mean value subtracted
	 */
	private Vector<Double> removeMean(Vector<Double> dataSet) {
		double mean = 0;
		for(int i=0; i<dataSet.size(); i++) {
			mean += dataSet.elementAt(i);
		}
		mean = mean/dataSet.size();
		
		for(int i=0; i<dataSet.size(); i++) {
			dataSet.set(i, dataSet.elementAt(i)-mean);
		}
		return dataSet;
	}
	
	
	/**
	 * Destroys activity
	 */
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
  	
		/**
		 * Asynchronous Task for reading the data points within the data file
		 * @author Caleb Ng (2015)
		 * //<Params, Progress, Result>
		 */
		class ReadFileService extends AsyncTask<Void, String, Boolean> {
			
			@Override
			protected Boolean doInBackground(Void... args) {		
				Scanner strings = null;
				InputStream stream = null;
				BufferedInputStream bstream = null; 
				ZipFile zipFile = null;
				try {
					System.out.println(externalStorageDirectory + Constants.APP_DIRECTORY + recordingName + Constants.ZIP_FILE_EXTENTION);
			  		File file = new File(externalStorageDirectory + Constants.APP_DIRECTORY, recordingName);
			  		zipFile = new ZipFile(file);
			  		Enumeration<? extends ZipEntry> entries = zipFile.entries();
			  				  		
			  		while (entries.hasMoreElements()) {
			  			ZipEntry zipEntry = entries.nextElement();
			  			stream = zipFile.getInputStream(zipEntry);
			  			strings = new Scanner(stream);			  			
			  			
			  			// Determine the start date and time from the header text
			  			System.out.println("Extracting recording start date and time.");
			  			Pattern pattern = Pattern.compile(".{184}");
			  			String extracted = strings.findInLine(pattern);
			  			pattern = Pattern.compile(recordingTimePattern);
			  			Matcher matcher = pattern.matcher(extracted);
			  			if (matcher.find()) {
			  				if (matcher.groupCount() == 6) {
				  				String Day = matcher.group(1);
				  				String Month = matcher.group(2);
				  				String Year = matcher.group(3);
				  				String Hour = matcher.group(4);
				  				String Minute = matcher.group(5);
				  				String Second = matcher.group(6);
				  				recordingDate = Month + "/" + Day + "/20" + Year;
				  				recordingTime = Hour + ":" + Minute + ":" + Second;
				  				graphTitle += " on " + recordingDate + " at " + recordingTime;

			  				}			  				
			  				else
			  					Log.e(DSGA_TAG, "ERROR: Insufficient number of matches found: " + matcher.groupCount());
			  			}
			  			
			  			// Use new line character as a delimiter for file data
			  			strings.nextLine();
			  			strings.useDelimiter("\n *");
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
					dataSetRAW.add(dataPoint);
				}
				System.out.println("Closing strings.");
				try {
//					bstream.close();
					stream.close();
					zipFile.close();
//					zipInput.close();
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
//				showDialog(progress_bar_type);
				prgDialog.setTitle("Opening File");
				prgDialog.setMessage("Opening " + recordingName + "\nPlease wait...");
				prgDialog.setCancelable(false);
				prgDialog.show();
				return;
			}
			
			protected void onPostExecute(Boolean readFileSuccess) {
				//called after doInBackground() has finished 
				// Check if the file was read successfully. If not, output error message and generate sample set of data
				if(!readFileSuccess) {
					
					Random randomGenerator = new Random();			
					System.out.println("@IOERROR: Unable to read from file. Creating random dataset");
					for(int i=0; i<100; i++)
				    {
						dataSetRAW.add(randomGenerator.nextDouble());
				    }
				}

				// Prepare data set for graphing
				dataSetRAW = removeMean(dataSetRAW);
				rawSeries = new GraphViewSeries(new GraphViewData[] {
		        });
				System.out.println("DSGA-TAG: Number of samples read is " + dataSetRAW.size());
				for (int i=0; i<dataSetRAW.size(); i++) {
				  	double pointX = i;
				  	double pointY = dataSetRAW.get(i);
				  	rawSeries.appendData(new GraphViewData(pointX, pointY), true, dataSetRAW.size());
				}
				graphData(dataSetRAW,100);
				prgDialog.dismiss();
				prgDialog = null;
				return;
			}
		}
		
		/**
		 * Asynchronous Task for calculating the Fourier Transform of the data set
		 * Will compute the Frequency spectrum as well as the Power spectrum 
		 * @author Caleb Ng (2015)
		 * //<Params, Progress, Result>
		 */
		class CalculateFFT extends AsyncTask<Boolean, String, postProcessParams> {
			@Override
			protected postProcessParams doInBackground(Boolean... args) {	
				// If Boolean = true, plot the Frequency spectrum
				// If Boolean = false, plot the Power spectrum
				Boolean freqSpectrum = args[0];
				int numSamples = dataSetRAW.size();
		  		double[] datapoints = new double[numSamples*2];
		  		int[] xIndex = new int[numSamples];
		  		for(int i=0; i<numSamples; i++) {
		  			datapoints[i] = (double) dataSetRAW.get(i);
		  			xIndex[i] = i;
		  		}
		  		System.out.println("Datapoint size: " + datapoints.length + " vs. Raw data size: " + numSamples);
		  		DoubleFFT_1D fft = new DoubleFFT_1D(numSamples);
		  		fft.realForwardFull(datapoints);

		  		fftSeries = new GraphViewSeries(new GraphViewData[] {});
		  		pwrSeries = new GraphViewSeries(new GraphViewData[] {});
		  		for(int i=0; i<numSamples/2+1; i++) {
		  			Complex c = new Complex(datapoints[2*i], datapoints[(2*i)+1]);
		  			double pointY = (double) c.abs();
		  			dataSetFFT.add(pointY);
		  			fftSeries.appendData(new GraphViewData(i,pointY), true, datapoints.length/2);
		  			double pointY_pwr = (double) 2*Math.pow(pointY, 2)/(samplingFrequency * numSamples);
					pointY_pwr = (double) 10*Math.log10(pointY_pwr);
		  			dataSetPWR.add(pointY_pwr);
		  			pwrSeries.appendData(new GraphViewData(i,pointY_pwr), true, datapoints.length/2);
		  			
		  		}
		  		
		  		System.out.println("##### DSGA ##### - N = " + numSamples + ";  PWR_N = " + dataSetPWR.size());

				postProcessParams returnParams = new postProcessParams();
				returnParams.readFilesSuccess = true;
				returnParams.freqSpectrum = freqSpectrum;
				return returnParams;
			}
			
			protected void onProgressUpdate(String...progress) {
				//called when the background task makes any progress
			}

			protected void onPreExecute() {
				//called before doInBackground() is started
				super.onPreExecute();
				// Show Progress Bar Dialog before calling doInBackground method
				prgDialog.setTitle("Processing Data");
				prgDialog.setMessage("Calculating the Fourier Transform\nPlease wait...");
				prgDialog.show();
				return;
			}
			
			protected void onPostExecute(postProcessParams returnParams) {
				//called after doInBackground() has finished 
				if(!returnParams.readFilesSuccess) {
					Log.e(TAG,"Unable to process data from file.");
					return;
				}
				else {
					if(returnParams.freqSpectrum)
						graphData(dataSetFFT, dataSetFFT.size());
					else
						graphData(dataSetPWR, dataSetPWR.size());
					prgDialog.dismiss();
					prgDialog = null;
					return;
				}				
			}
		}
		
		private class postProcessParams {
			public boolean readFilesSuccess;
			public boolean freqSpectrum;
		}
	}

