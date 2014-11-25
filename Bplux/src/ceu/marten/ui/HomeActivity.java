//Modified by Brittaney Geisler November 2014

package ceu.marten.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.PopupMenu;
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
	public static String btName;
	
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
			String recname1 = recname + " " + currentDateandTime;//dateFormat.format(date);
			Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
			newRecordingIntent.putExtra(ConfigurationsActivity.KEY_RECORDING_NAME, recname1);
			newRecordingIntent.putExtra(ConfigurationsActivity.KEY_CONFIGURATION, ConfigurationsActivity.configurations.get(ConfigurationsActivity.configurationClickedPosition));
			startActivity(newRecordingIntent);
			overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		}
		else if (!configset && !nameset) Toast.makeText(getApplicationContext(), "Please Set Configurations and Patient Name First",Toast.LENGTH_SHORT).show();
		else if (!configset) Toast.makeText(getApplicationContext(), "Please Set Configurations First",Toast.LENGTH_SHORT).show();
		else Toast.makeText(getApplicationContext(), "Please Set Patient Name First",Toast.LENGTH_SHORT).show();
	}

	public void onClickedSavedData(View view) {
		startActivity(new Intent(this, RecordingsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onClickedConfiguration(View view) {
		startActivity(new Intent(this, ConfigurationsActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
