package com.ubc.capstonegroup70;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import ceu.marten.bitadroid.R;
import ceu.marten.model.Constants;
import ceu.marten.model.DeviceRecording;
import ceu.marten.ui.RecordingsActivity;
import ceu.marten.ui.adapters.RecordingsListAdapter;

import com.j256.ormlite.dao.Dao;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Functions for exporting and deleting recording files.
 * Adapted from ceu.marten.ui.RecordingsActivity
 * 
 * @author Caleb Ng
 *
 */
public class FileServices {

	/**
	 * Class variables
	 */
	private static final String TAG = RecordingsActivity.class.getName();

	private ListView recordingsLV;
	private String recordingName;
	private RecordingsListAdapter baseAdapter;
	private ArrayList<DeviceRecording> recordings = null;
	private int[] reverseSortedPositions;
	private SharedPreferences.Editor prefEditor = null;
	private LayoutInflater inflater;
	private static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
	//DIALOGS
	private AlertDialog confirmationDialog, errorDialog;

	

}
