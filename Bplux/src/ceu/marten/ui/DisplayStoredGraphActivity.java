package ceu.marten.ui;

import java.util.Vector;
import java.util.Collections;
import java.util.Random;

//import com.example.bluetoothnew.R;
import ceu.marten.bitadroid.R;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.os.Bundle;
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
  private Vector<Double> testData = new Vector<Double>(); 
  private Vector<Double> dataSet = new Vector<Double>();

  int n = 0;
  int max=0;
  int min=0;
  
  public static double [] buffer;
private double[] calculateRange() {
  	double dataRange[] = {0, 0}; //{y-min, y-max}
  	Object min = Collections.max(testData);
  	Object max = Collections.min(testData);
  	dataRange[0] = (double) min;
  	dataRange[1] = (double) max;    	
  	return dataRange;
  }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Intent intent = getIntent();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_stored_graph_activity);
		Random randomGenerator = new Random();
		double yBounds[] = {0,0};
		
		for(int i=0; i<100; i++)
	    {
			testData.add(randomGenerator.nextDouble());
	    }
			
      exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
      });
		
      for (int i=0; i<testData.size(); i++) {
      	double pointX = i;
      	double pointY = testData.get(i);
      	exampleSeries1.appendData(new GraphViewData(pointX, pointY), true, 1024);
      	System.out.println("X = " + pointX + ", Y = " + pointY);
      }
      
      System.out.println("Defining data set.");
      graphView = new LineGraphView(
          this 
          , "EMG Test Data"
      );
      
      // Calculate range of y values
      yBounds = calculateRange();
      // Calculate the appropriate max value and interval level of labels in y-direction
     	max = (int) yBounds[1] + 1;

      int yInterval;
      if (max <= 10) {
      	yInterval = 1;
      }
      else if (max <= 55) {
      	yInterval = 5;
      }
      else if (max <=110) {
      	yInterval = 10;
      }
      else {
      	yInterval = 20;
      }
      int yLabel = max;
      while (yLabel % yInterval != 0) {
      	yLabel++;
      }
      // Calculate the appropriate min value in y-direction
	    if (yBounds[0] >= 0) {    
      	min = (int) yBounds[1];
	    }
	    else {
	        min = (int) yBounds[1] - 1;
	    }
	    
	    // Calculate appropriate interval value in x-direction
      int xInterval;
      int xMax = testData.size();
      if (xMax <= 10) {
      	xInterval = 1;
      }
      else if (xMax <= 55) {
      	xInterval = 5;
      }
      else if (xMax <=110) {
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
      ((LineGraphView) graphView).setDrawDataPoints(true);
      
      graphView.setScalable(true);  
      graphView.setScrollable(true);
      graphView.setViewPort(0, 10);
//      graphView.setViewPort(0,testData.size());
      graphView.setManualYAxisBounds(max, min);
//      graphView.setManualYAxisBounds(yBounds[1],yBounds[0]);
      graphView.getGraphViewStyle().setNumVerticalLabels((yLabel/yInterval)*10 + 1);
      graphView.getGraphViewStyle().setNumHorizontalLabels(xLabel/xInterval + 1);
      graphView.getGraphViewStyle().setGridColor(Color.BLACK);
      graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
      graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
      graphView.getGraphViewStyle().setVerticalLabelsWidth(50);

      LinearLayout layout = (LinearLayout) findViewById(R.id.dataGraph);
      layout.addView(graphView);
  }
}
