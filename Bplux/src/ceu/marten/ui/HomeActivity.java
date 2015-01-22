//Modified by Brittaney Geisler November 2014

package ceu.marten.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.mburman.fileexplore.FileExplore;
//import com.mburman.fileexplore.FileExplore.Item;
import com.ubc.capstonegroup70.DisplayStoredGraphActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.model.DeviceConfiguration;
import ceu.marten.model.DeviceRecording;
import ceu.marten.ui.dialogs.AboutDialog;
import ceu.marten.ui.dialogs.HelpDialog;

public class HomeActivity extends Activity {//implements android.widget.PopupMenu.OnMenuItemClickListener {

	public static boolean configset = false;
	public static boolean nameset = false;
	public static String recname = "DEFAULT";
	public int i=1;
	public static String btName = "EMG_Sensor";
	public static String recname1;
	public static int sfValue = 100;
	private DeviceConfiguration newConfiguration;
	private String[]  activeChannels = {"EMG"};
	
	// Variables for file explorer
	private final int REQUEST_CODE_PICK_DIR = 1;
	private final int REQUEST_CODE_PICK_FILE = 2;
	
	// Stores names of traversed directories
	  ArrayList<String> str = new ArrayList<String>();

	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
	
	private static final String TAG = "F_PATH";
	
	private Item[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory() + "/Bioplux");
	private String chosenFile;
	private static final int DIALOG_LOAD_FILE = 1000;
	
	ListAdapter adapter;
	
	/*@Override
	public boolean onMenuItemClick(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.gm_settings:
	        	Intent globalSettingsIntent = new Intent(this, SettingsActivity.class);
	        	globalSettingsIntent.putExtra(Constants.KEY_SETTINGS_TYPE, 1);
	        	startActivity(globalSettingsIntent);
	            return true;
	       
	        case R.id.gm_help:
	        	HelpDialog help = new HelpDialog(this);
	        	help.setTitle(getString(R.string.gm_help_title));
	        	help.setCanceledOnTouchOutside(true);
	        	help.show();
	        	return true;
	        	
	        case R.id.gm_about:
	        	AboutDialog about = new AboutDialog(this);
	        	about.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        	about.setCanceledOnTouchOutside(true);
	        	about.show();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ly_home);
	}


	/************************ BUTTON EVENTS *******************/
	
	/*public void onClikedMenuItems(View v) {
	    PopupMenu popup = new PopupMenu(this, v);
	    popup.setOnMenuItemClickListener(this);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.global_menu, popup.getMenu());
	    popup.show();
	}*/
	
	public void onClickedSetPatientName(View view) {
		setPatientNameDialog(view);
	}
	
	private void setPatientNameDialog(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Enter Patient Name: ");
		final EditText input = new EditText(this);
		alertDialogBuilder.setView(input);
		input.setText(recname);
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  Editable value = input.getText();
				  recname = value.toString();
				  nameset = true;
				  //BitalinoAndroid.btName = recname;
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			   
			  }
		});
		alertDialogBuilder.show();	
	}


	public void onClickedStart(View view) {
		if (configset && nameset){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDateandTime = sdf.format(new Date());
			//DateFormat dateFormat = DateFormat.getDateTimeInstance();
			//Date date = new Date();
			recname1 = recname + " " + currentDateandTime;//dateFormat.format(date);
			Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
			//newRecordingIntent.putExtra(ConfigurationsActivity.KEY_RECORDING_NAME, recname1);
			//newRecordingIntent.putExtra(ConfigurationsActivity.KEY_CONFIGURATION, ConfigurationsActivity.configurations.get(ConfigurationsActivity.configurationClickedPosition));
			newRecordingIntent.putExtra("recordingName", recname1);
			newRecordingIntent.putExtra("configuration", newConfiguration);
			
			//newRecordingIntent.putExtra("configuration", ConfigurationsActivity.configurations.get(0));
			startActivity(newRecordingIntent);
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		}
		else if (!configset && !nameset) Toast.makeText(getApplicationContext(), "Please Set Configurations and Patient Name First",Toast.LENGTH_SHORT).show();
		else if (!configset) Toast.makeText(getApplicationContext(), "Please Set Configurations First",Toast.LENGTH_SHORT).show();
		else Toast.makeText(getApplicationContext(), "Please Set Patient Name First",Toast.LENGTH_SHORT).show();
	}

	public void onClickedSavedData(View view) {
//		startActivity(new Intent(this, RecordingsActivity.class));
		
		/*// Start FileBrowserActivity
		Intent fileExploreIntent = new Intent(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.INTENT_ACTION_SELECT_DIR, null, 
				this, ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.class);
		fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter, "/sdcard/Bioplux");
		startActivityForResult(
				fileExploreIntent,
				REQUEST_CODE_PICK_FILE
		);*/
		
		// Start FileExplorer
//		startActivity(new Intent(this, FileExplore.class));
		loadFileList();
	    showDialog(DIALOG_LOAD_FILE);

		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onClickedConfiguration(View view) {
		//startActivity(new Intent(this, ConfigurationsActivity.class));
		//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		
		setConfigDialog(view);
	}
	/*public void onClickedtest(View view){
		startActivity(new Intent(this, ConfigurationsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				
	}
	*/
	private void setConfigDialog(View view){	
		
		newConfiguration = new DeviceConfiguration(this);
		newConfiguration.setNumberOfBits(12);
		newConfiguration.setVisualizationFrequency(100);
		newConfiguration.setActiveChannels(activeChannels);
		newConfiguration.setDisplayChannels(activeChannels);
		newConfiguration.setName("MYconfig");
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
		newConfiguration.setCreateDate(dateFormat.format(date));
		
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Configuration:");
		Context context = this;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		final TextView Title1 = new TextView(context);
		Title1.setText("Bluetooth Name");
		layout.addView(Title1);
		
		final EditText BTNamebox = new EditText(context);
		BTNamebox.setText(btName);
		layout.addView(BTNamebox);
		
		final TextView Title2 = new TextView(context);
		Title2.setText("Sampling Frequency");
		layout.addView(Title2);
		
		final SeekBar sBar = new SeekBar(context);
		sBar.setMax(1000);
		if (sfValue == 1) sBar.setProgress(1);
		else if (sfValue == 10) sBar.setProgress(333);
		else if (sfValue == 100) sBar.setProgress(666);
		else if (sfValue == 1000) sBar.setProgress(1000);
		layout.addView(sBar);
		
		final TextView Title3 = new TextView(context);
		String string = Integer.toString(sfValue);
		Title3.setText(string);
		layout.addView(Title3);
		
		sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	            if (progress>=1 && progress<250) sfValue = 1;
	            else if (progress>=250 && progress<500) sfValue = 10;
	            else if (progress>=500 && progress<750) sfValue = 100;
	            else if (progress>=750 && progress<=1000) sfValue = 1000;   
	            
	    		String string = Integer.toString(sfValue);
	    		Title3.setText(string);
	            
	        }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
	    });

		alertDialogBuilder.setView(layout);
		
		alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  Editable BTName = BTNamebox.getText();
				  btName = BTName.toString();
				  configset = true;
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
	
	// Open FileExplore
	private void loadFileList() {
	    try {
	      path.mkdirs();
	    } catch (SecurityException e) {
	      Log.e(TAG, "unable to write on the sd card ");
	    }

	    // Checks whether path exists
	    if (path.exists()) {
	      FilenameFilter filter = new FilenameFilter() {
	        @Override
	        public boolean accept(File dir, String filename) {
	          File sel = new File(dir, filename);
	          // Filters based on whether the file is hidden or not
	          return (sel.isFile() || sel.isDirectory())
	              && !sel.isHidden();

	        }
	      };

	      String[] fList = path.list(filter);
	      fileList = new Item[fList.length];
	      for (int i = 0; i < fList.length; i++) {
	        fileList[i] = new Item(fList[i], R.drawable.file_icon);

	        // Convert into file path
	        File sel = new File(path, fList[i]);

	        // Set drawables
	        if (sel.isDirectory()) {
	          fileList[i].icon = R.drawable.directory_icon;
	          Log.d("DIRECTORY", fileList[i].file);
	        } else {
	          Log.d("FILE", fileList[i].file);
	        }
	      }

	      if (!firstLvl) {
	        Item temp[] = new Item[fileList.length + 1];
	        for (int i = 0; i < fileList.length; i++) {
	          temp[i + 1] = fileList[i];
	        }
	        temp[0] = new Item("Up", R.drawable.directory_up);
	        fileList = temp;
	      }
	    } else {
	      Log.e(TAG, "path does not exist");
	    }

	    adapter = new ArrayAdapter<Item>(this,
	        android.R.layout.select_dialog_item, android.R.id.text1,
	        fileList) {
	      @Override
	      public View getView(int position, View convertView, ViewGroup parent) {
	        // creates view
	        View view = super.getView(position, convertView, parent);
	        TextView textView = (TextView) view
	            .findViewById(android.R.id.text1);

	        // put the image on the text view
	        textView.setCompoundDrawablesWithIntrinsicBounds(
	            fileList[position].icon, 0, 0, 0);

	        // add margin between image and text (support various screen
	        // densities)
	        int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
	        textView.setCompoundDrawablePadding(dp5);

	        return view;
	      }
	    };

	  }

	  private class Item {
	    public String file;
	    public int icon;

	    public Item(String file, Integer icon) {
	      this.file = file;
	      this.icon = icon;
	    }

	    @Override
	    public String toString() {
	      return file;
	    }
	  }
	
	@Override
	  protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    AlertDialog.Builder builder = new Builder(this);

	    if (fileList == null) {
	      Log.e(TAG, "No files loaded");
	      dialog = builder.create();
	      return dialog;
	    }
	    
	    switch (id) {
	    case DIALOG_LOAD_FILE:
	      builder.setTitle("Choose your file");
	      builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	          chosenFile = fileList[which].file;
	          File sel = new File(path + "/" + chosenFile);
	          if (sel.isDirectory()) {
	            firstLvl = false;

	            // Adds chosen directory to list
	            str.add(chosenFile);
	            fileList = null;
	            path = new File(sel + "");

	            loadFileList();

	            removeDialog(DIALOG_LOAD_FILE);
	            showDialog(DIALOG_LOAD_FILE);
	            Log.d(TAG, path.getAbsolutePath());

	          }

	          // Checks if 'up' was clicked
	          else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

	            // present directory removed from list
	            String s = str.remove(str.size() - 1);

	            // path modified to exclude present directory
	            path = new File(path.toString().substring(0,
	                path.toString().lastIndexOf(s)));
	            fileList = null;

	            // if there are no more directories in the list, then
	            // its the first level
	            if (str.isEmpty()) {
	              firstLvl = true;
	            }
	            loadFileList();

	            removeDialog(DIALOG_LOAD_FILE);
	            showDialog(DIALOG_LOAD_FILE);
	            Log.d(TAG, path.getAbsolutePath());

	          }
	          // File picked
	          else {
	            // Perform action with file picked
	            System.out.println("FILE EXPLORE: Chosen file is: " + chosenFile);
	            Intent intent = new Intent(HomeActivity.this, DisplayStoredGraphActivity.class);
	            intent.putExtra("FILE_NAME", chosenFile);
	            startActivity(intent);
	          }

	        }
	      });
	      break;
	    }
	    /*public boolean onLongClick(DialogInterface dialog, int which) {
	    	
	    }*/
	    dialog = builder.show();
	    /*dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
	        @Override
	        public void onDismiss(final DialogInterface arg0) {
	            // do something
	        	finish();
	        }
	    });*/
	    return dialog;
	  }
}
