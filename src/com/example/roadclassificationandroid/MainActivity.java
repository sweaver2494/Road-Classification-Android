/*
 *
 * @author Scott Weaver
 */
package com.example.roadclassificationandroid;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity {

	String condition = "";
	String speed = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final DataCollection collection = new DataCollection(this);
		final EditText classificationText = (EditText) findViewById(R.id.classificationText);
		

		//CONDITION DROP DOWN
		ArrayList<String> conditions = new ArrayList<String>();
		conditions.add("asphalt_rough");
		conditions.add("asphalt_smooth");
		conditions.add("bumps_left");
		conditions.add("bumps_right");
		conditions.add("concrete");
		conditions.add("dirt");
		conditions.add("gravel");
		ArrayAdapter<String> conditionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, conditions);
		conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner conditionSpinner = (Spinner) findViewById(R.id.conditionSpinner);
		conditionSpinner.setAdapter(conditionAdapter);
		conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				condition = parent.getItemAtPosition(pos).toString();
			    classificationText.setText(condition + "_" + speed);
			}
			
			public void onNothingSelected(AdapterView<?> parent) {
				condition = "";
			    classificationText.setText(condition + "_" + speed);
			}
		});
		
		//SPEED DROP DOWN
		ArrayList<String> speeds = new ArrayList<String>();
		speeds.add("30");
		speeds.add("60");
		ArrayAdapter<String> speedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speeds);
		speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner speedSpinner = (Spinner) findViewById(R.id.speedSpinner);
		speedSpinner.setAdapter(speedAdapter);
		speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				speed = parent.getItemAtPosition(pos).toString();
			    classificationText.setText(condition + "_" + speed);
			}
			
			public void onNothingSelected(AdapterView<?> parent) {
				speed = "";
			    classificationText.setText(condition + "_" + speed);
			}
		});
		
		//STOP BUTTON
		((Button) findViewById(R.id.stopButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (collection.StopCollection()) {
					((Chronometer) findViewById(R.id.chronometer)).stop();

					Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent.setData(Uri.fromFile(new File(collection.getDataFilePath())));
					sendBroadcast(intent);
				}
			}
		});
		
		//START BUTTON
		((Button) findViewById(R.id.startButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				if (collection.StartCollection()) {
					((Chronometer) findViewById(R.id.chronometer)).setBase(SystemClock.elapsedRealtime());
					((Chronometer) findViewById(R.id.chronometer)).start();

					String filePath = collection.getDataFilePath();
					System.out.println("Data File: " + filePath);
					((EditText) findViewById(R.id.filenameText)).setText(filePath);
					
					Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent.setData(Uri.fromFile(new File(collection.getDataFilePath())));
					sendBroadcast(intent);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
