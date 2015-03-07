//Modified by Brittaney Geisler November 2014

package ceu.marten.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.model.DeviceConfiguration;

import com.ubc.capstonegroup70.DisplayStoredGraphActivity;
//import com.mburman.fileexplore.FileExplore.Item;

public class HomeActivity extends Activity {//implements android.widget.PopupMenu.OnMenuItemClickListener {

	public static boolean configset = false;
	public static boolean nameset = false;
	public static String recname = "DEFAULT";
	public int i=1;
	public static String btName = "bitalino";//"EMG_Sensor";
	public static String recname1;
	public static int sfValue = 100;
	private DeviceConfiguration newConfiguration;
	private String[]  activeChannels = {"EMG"};
	public static int freq;
	
	// Variables for file explorer
	private final int REQUEST_CODE_PICK_DIR = 1;
	private final int REQUEST_CODE_PICK_FILE = 2;
	
	// Stores names of traversed directories
	  ArrayList<String> str = new ArrayList<String>();

	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
	
	private static final String TAG = "F_PATH";
	
	private Item[] fileList;
	private final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	private File path = new File(Environment.getExternalStorageDirectory() + "/Bioplux");
	private String chosenFile;
	private static final int DIALOG_LOAD_FILE = 1000;
	private static final int DIALOG_EXPORT = 1001;
	private static final int DIALOG_DELETE = 1002;
	
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
		// Start FileExplorer
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
		//newConfiguration.setVisualizationFrequency(1000);
		//newConfiguration.setSamplingFrequency(1000);
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
		
		/*final TextView Title2 = new TextView(context);
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
	    });*/

		alertDialogBuilder.setView(layout);
		
		alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  Editable BTName = BTNamebox.getText();
				  btName = BTName.toString();
				  configset = true;
				  newConfiguration.setVisualizationFrequency(1000);
				  newConfiguration.setSamplingFrequency(100);
				  newConfiguration.setMacAddress(btName);
				  //freq = sfValue;
				  
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			   
			  }
		});
		alertDialogBuilder.show();	
		
	}
	
	/*
	 * Android File Explore
	 * 
	 * Copyright 2011 Manish Burman
	 * Modified by Caleb Ng 2015
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
	 * compliance with the License. You may obtain a copy of the License at
	 * 		http://www.apache.org/licenses/LICENSE-2.0
	 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
	 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	 * See the License for the specific language governing permissions and limitations under the License.
	 */
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
	  
	  /*
	   * Convert an Item array into a CharSequence array
	   */
	  private CharSequence[] itemToSequence(Item[] list) {
		  CharSequence[] chars = new CharSequence[list.length];
		  for(int i=0; i<list.length; i++) {
			  chars[i] = list[i].toString();
		  }
		  
		  return chars;
	  }

	
	@Override
	  protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
	    AlertDialog.Builder builder = new Builder(this);
	    
	    onPrepareDialog(id, dialog);

	    return dialog;
	  }
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		final File externalStorageDirectory = Environment.getExternalStorageDirectory();
		final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
		final AlertDialog.Builder builder = new Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		
		if (fileList == null) {
	      Log.e(TAG, "No files loaded");
	      dialog = builder.create();
	    }
//		builder.setView(inflater.inflate(R.layout.dialog_stored_recordings, null));
				
	    switch (id) {
	    case DIALOG_LOAD_FILE:
	      builder.setTitle("Choose the recording you wish to open:");
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
	      
	      /**
	       * Added buttons for implementing exporting and deleting functionality
	       */
	      builder.setPositiveButton(R.string.export_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// If user clicks on the Export button, allow users to select files that they wish to export
					removeDialog(DIALOG_LOAD_FILE);
		            showDialog(DIALOG_EXPORT);
					
				}
			});
			builder.setNegativeButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// If user clicks on the Delete button, allow users to select files that they wish to delete
					removeDialog(DIALOG_LOAD_FILE);
		            showDialog(DIALOG_DELETE);					
				}
			});
	      break;
	      /**
	       * Added functionality for Exporting recordings
	       * @author Caleb Ng
	       */
	    case DIALOG_EXPORT:
	    	builder.setTitle("Choose the recording(s) you wish to export:");
	    	builder.setMultiChoiceItems(itemToSequence(fileList), null, 
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							loadFileList();
							if(isChecked) {
								// If user checked this item, add this to list of files to export
								mSelectedItems.add(which);
							} else if(mSelectedItems.contains(which)) {
								// If the user selects to remove a checked item, then remove from list of files to export
								mSelectedItems.remove(Integer.valueOf(which));
							}
						}
					});
			// Set text of positive button to "Confirm"
			builder.setPositiveButton(R.string.confirm_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of positive button, allow user to export selected files
					/*if(mSelectedItems.size() <= 0) {
						removeDialog(DIALOG_EXPORT);
			            showDialog(DIALOG_LOAD_FILE);
					}
					else if(mSelectedItems.size() == 1) {
						File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(0)]);
						sendSingleRecording(recordingZipFile);
					}
					else {
						sendMultipleRecordings(mSelectedItems);
					}*/
					sendRecordings(mSelectedItems);
					
					/*loadFileList();
					if(isChecked) {
						// If user checked this item, add this to list of files to export
						mSelectedItems.add(which);
					} else if(mSelectedItems.contains(which)) {
						// If the user selects to remove a checked item, then remove from list of files to export
						mSelectedItems.remove(Integer.valueOf(which));
					}*/
				}
			});			
			// Set text of negative button to "Cancel"
			builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of negative button, change dialog back to a single-choice list.
					removeDialog(DIALOG_EXPORT);
		            showDialog(DIALOG_LOAD_FILE);						
				}
			});			
			break;
			/**
			 * Added functionality for deleting recordings
			 * @author Caleb Ng
			 */
	    case DIALOG_DELETE:
	    	builder.setTitle("Choose the recording(s) you wish to delete:");
	    	builder.setMultiChoiceItems(itemToSequence(fileList), null, 
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							
						}
			});
			// Set text of positive button to "Confirm"
			builder.setPositiveButton(R.string.confirm_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// If only 1 item selected, no further work is required to prep files for sending
					
					
				}
			});			
			// Set text of negative button to "Cancel"
			builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// On click of negative button, change dialog back to a single-choice list.
					removeDialog(DIALOG_DELETE);
		            showDialog(DIALOG_LOAD_FILE);						
				}
			});			
			break;
	    	
	    }
	    
	    
	    dialog = builder.show();
	}
	// End of FileExplore
	
	// File Export functions adapted from ceu.marten.services.RecordingActivity
	// @author Caleb Ng
	/**
	 * True if the selection of files is larger than 20 MB. False otherwise.
	 * @author Caleb Ng
	 */
	private boolean selectionSizeBiggerThan20Mb(ArrayList<Integer> mSelectedItems) {
		double summedSize = 0;
		for(int i=0; i<mSelectedItems.size(); i++) {
			File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(i)]);
			summedSize += (recordingZipFile.length() / 1024d) / 1024d;
		}
		if(summedSize > 20.0d)
			return true;
		else
			return false;
	}
	/**
	 * Export selected files
	 * @author Caleb Ng
	 */
	private void sendRecordings(ArrayList<Integer> mSelectedItems) {
		
		if(mSelectedItems.size() <= 0) {
			// No items selected, display a toast notification to notify user
			removeDialog(DIALOG_EXPORT);
            showDialog(DIALOG_EXPORT);
			Toast toast = Toast.makeText(getApplicationContext(), "No items selected", Toast.LENGTH_SHORT);
			toast.show();			
		}
		else {
			// Check if size of selected items exceed 20MB
			if(selectionSizeBiggerThan20Mb(mSelectedItems)) {
				// Selection size is too large, display a toast notification to notify user
				Toast toast = Toast.makeText(getApplicationContext(), "Selection size exceeds 20MB limit", Toast.LENGTH_SHORT);
				toast.show();
			}
			else {
				Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				sendIntent.setType("application/zip");
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for(int i=0; i<mSelectedItems.size(); i++) {
					File recordingZipFile = new File(externalStorageDirectory + Constants.APP_DIRECTORY + fileList[mSelectedItems.get(i)]);
					// gets recording file Uri
					Uri fileUri = Uri.fromFile(recordingZipFile);
					uris.add(fileUri);
				}
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				startActivity(Intent.createChooser(sendIntent, getString(R.string.ra_send_dialog_title)));
			}
		}		
	}
	
}
