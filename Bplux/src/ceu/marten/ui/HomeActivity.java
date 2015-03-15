//Modified by Brittaney Geisler March 2015

package ceu.marten.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import ceu.marten.bitadroid.R;
import ceu.marten.model.DeviceConfiguration;
//import com.mburman.fileexplore.FileExplore.Item;

public class HomeActivity extends Activity {//implements android.widget.PopupMenu.OnMenuItemClickListener {

	public static boolean configset = false;
	public static boolean nameset = false;
	public static String PatientName = "Brittaney";
	public int i=1;
	public static String btName = "bitalino";//"EMG_Sensor";
	private DeviceConfiguration newConfiguration;
	private String[]  activeChannels = {"EMG"};
	private String[] spinner_array = new String[20];
	private int spinner_array_count;
	Context context = this;
	private boolean remove_patient = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ly_home);
		final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter SpinnerAdapter =  new ArrayAdapter(this, android.R.layout.simple_spinner_item,  spinner_array);
		SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(SpinnerAdapter);
		spinner_array[0] = "--  Patient Name  --";
		spinner_array[1] = "-- Add New Patient --";
		spinner_array[2] = "-- Remove Patient --";
		spinner_array_count = 3;
		for (int j=3; j<20; j++){
			spinner_array[j] = " ";
		}
		try {
			readNamesFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	


	/************************ BUTTON EVENTS *******************/
	
	/*public void onClikedMenuItems(View v) {
	    PopupMenu popup = new PopupMenu(this, v);
	    popup.setOnMenuItemClickListener(this);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.global_menu, popup.getMenu());
	    popup.show();
	}*/
		
	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {                

        	if (position == 0) {
        		nameset = false;
        		remove_patient = false;
        	}
        	else if (position == 1){
        		AddNewPatientDialog(view);
        		spinner.setSelection(0);
        		nameset = false;
        		remove_patient = false;
        	}
        	else if (position == 2){
        		remove_patient = true;
        		spinner.performClick();
        		for (int j=0; j<2; j++)
        		Toast.makeText(getApplicationContext(), "         SELECT PATIENT TO REMOVE\nOR PRESS --Patient Name-- TO CANCEL",Toast.LENGTH_LONG).show();
        	}
        	else if (position >= spinner_array_count){
        		nameset = false;
        		spinner.setSelection(0);
        		remove_patient = false;
        	}
        	else {
        		if (remove_patient){
        			
        			File file = new File("/storage/emulated/0/"+spinner_array[position]+"INFO"+".txt");
        			if(file.exists()) {
        				file.delete();
        			}
        			
        			spinner_array[position] = " ";
        			remove_patient = false;
        			spinner.setSelection(0);
        			for (int j=position; j<spinner_array_count; j++){
        				spinner_array[j] = spinner_array[j+1];
        			}
        			removePatientFromFile(position-3);
        			spinner_array_count--;
        			
        		}
        		else {
        			PatientName = (String) spinner.getItemAtPosition(position);
        			nameset = true;
        		}
            }
            
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        	
        }
    });  
	

	
	}
	
	public void onClickedStart(View view) {
		if (configset && nameset){
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//String currentDateandTime = sdf.format(new Date());
			//recname1 = recname + " " + currentDateandTime;
			Intent patientSessionIntent = new Intent(this, PatientSessionActivity.class);
			//newRecordingIntent.putExtra(ConfigurationsActivity.KEY_RECORDING_NAME, recname1);
			//newRecordingIntent.putExtra(ConfigurationsActivity.KEY_CONFIGURATION, ConfigurationsActivity.configurations.get(ConfigurationsActivity.configurationClickedPosition));
			patientSessionIntent.putExtra("patientName", PatientName);
			patientSessionIntent.putExtra("configuration", newConfiguration);
			startActivity(patientSessionIntent);
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		}
		else if (configset) Toast.makeText(getApplicationContext(), "NO PATIENT SELECTED",Toast.LENGTH_SHORT).show();
		else if (nameset) Toast.makeText(getApplicationContext(), "NO BLUETOOTH SELECTED",Toast.LENGTH_SHORT).show();
		else Toast.makeText(getApplicationContext(), "NO BLUETOOTH OR PATIENT SELECTED",Toast.LENGTH_SHORT).show();
	}
	
	public void onClickedConfiguration(View view) {
		setConfigDialog(view);
	}
	
	private void setConfigDialog(View view){	
		
		newConfiguration = new DeviceConfiguration(this);
		newConfiguration.setNumberOfBits(12);
		newConfiguration.setActiveChannels(activeChannels);
		newConfiguration.setDisplayChannels(activeChannels);
		newConfiguration.setName("MYconfig");
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
		newConfiguration.setCreateDate(dateFormat.format(date));
		
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Bluetooth Name");
		Context context = this;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		final EditText BTNamebox = new EditText(context);
		BTNamebox.setText(btName);
		layout.addView(BTNamebox);

		alertDialogBuilder.setView(layout);
		
		alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  Editable BTName = BTNamebox.getText();
				  btName = BTName.toString();
				  configset = true;
				  newConfiguration.setVisualizationFrequency(1000);
				  newConfiguration.setSamplingFrequency(100);
				  newConfiguration.setMacAddress(btName);
				  
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			   
			  }
		});
		alertDialogBuilder.show();	
		
	}
	private void AddNewPatientDialog(View view){	
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Patient Name");
		Context context = this;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		final EditText PNamebox = new EditText(context);
		layout.addView(PNamebox);
		
		
		alertDialogBuilder.setView(layout);
		
		alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  Editable PName = PNamebox.getText();
				  spinner_array[spinner_array_count] = PName.toString();
				  spinner_array_count++;
				  
				  try {
					saveNameToFile(PName.toString());
				  } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				  }
				  
				  
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			   
			  }
		});
		alertDialogBuilder.show();	
		
	}
	
	private void saveNameToFile(String patientName) throws IOException{
		
		//WRITE 
		
		String write_string = patientName + '\n';
		
		try {
			File file = new File("/storage/emulated/0/patientNames.txt");
			if (!file.exists()) {
				//Toast.makeText(context, "FIRST_EXISTS", Toast.LENGTH_SHORT).show();
				file = new File(Environment.getExternalStorageDirectory(),"patientNames.txt");
			}
			//Toast.makeText(context, file.getAbsolutePath(), Toast.LENGTH_LONG).show();
			FileOutputStream outputStream = openFileOutput("patientNames.txt", Context.MODE_APPEND);
			outputStream = new FileOutputStream(file, true);
        
			outputStream.write(write_string.getBytes());//patientName.getBytes());
			outputStream.flush();
		    outputStream.close();
			        
		
		} catch (IOException e) {
			    e.printStackTrace();
			
		}
		//readNamesFromFile();

	}
	private void readNamesFromFile() throws IOException{
		
		//READ
		try {
			//num_of_patients = 0;
			FileInputStream fIn = new FileInputStream("/storage/emulated/0/patientNames.txt");
		    @SuppressWarnings("resource")
			Scanner scanner = new Scanner(fIn);
		    String readString = null;
		    boolean first_read = true;
		    while (scanner.hasNextLine())
		    {
		    	//num_of_patients ++;
		    	
		        String currentline = scanner.nextLine();
		        if (first_read == true) {
		        	readString = currentline;
		        	first_read = false;
		        }
		        else readString = readString + currentline;
		        //spinner_array[num_of_patients+1] = currentline;
		        spinner_array[spinner_array_count] = currentline;
		        spinner_array_count++;
		    }
		    //Toast.makeText(context, readString, Toast.LENGTH_SHORT).show();
		        
		} catch (IOException ioe) 
		    {ioe.printStackTrace();}
		
	}
	
	private void removePatientFromFile(int position){
		
		//READ
		boolean first_write = true;
	    String[] temp_array = new String[10];//temp array
	    int temp_count = 0;//keeps track of location in array
	    int total_count = 0;
	    for (int j=0; j<10; j++){
	    	temp_array[j] = null;
	    }
		try {
			FileInputStream fIn = new FileInputStream("/storage/emulated/0/patientNames.txt");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(fIn);
			while (scanner.hasNextLine())
			{
		    	String currentline = scanner.nextLine();
			  	if (temp_count < position){
			  		temp_array[temp_count] = currentline;
					temp_count++;
					total_count++;
		    	}
			  	else if (temp_count == position){
			  		temp_count++;
			  	}
			  	else if (temp_count > position){
			  		temp_array[temp_count-1] = currentline;
			  		temp_count++;
			  		total_count++;
			  	}
			  	//Toast.makeText(context, temp_array[temp_count-1], Toast.LENGTH_SHORT).show();
		    }  	

		} catch (IOException ioe) 
			 {ioe.printStackTrace();}
				
		//WRITE 
			
		String write_string=null;
				
		for (int j=0; j<total_count; j++){
				write_string = temp_array[j]+'\n';
				try {
					File file = new File("/storage/emulated/0/patientNames.txt");
					if (!file.exists()) {
						file = new File(Environment.getExternalStorageDirectory(),"patientNames.txt");
					}
					FileOutputStream outputStream = openFileOutput("patientNames.txt", Context.MODE_APPEND);
					if (first_write) {
						outputStream = new FileOutputStream(file, false);
						first_write = false;
					}
					else outputStream = new FileOutputStream(file, true);
					outputStream.write(write_string.getBytes());
					outputStream.flush();
				    outputStream.close();			
				} catch (IOException e) {
					    e.printStackTrace();
				}
		}
	}

}