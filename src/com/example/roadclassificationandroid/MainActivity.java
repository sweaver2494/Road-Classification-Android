package com.example.roadclassificationandroid;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final DataCollection collection = new DataCollection(this);
		
		((Button) findViewById(R.id.stopButton))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (collection.StopCollection()) {
							((Chronometer) findViewById(R.id.chronometer)).stop();
							
							String filePath = "Data File: " + collection.getDataFilePath();
							((EditText) findViewById(R.id.classificationText)).setText(filePath);
							
							Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
							intent.setData(Uri.fromFile(new File(collection.getDataFilePath())));
							sendBroadcast(intent);
						}
					}
				});
		((Button) findViewById(R.id.startButton))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						if (collection.StartCollection()) {
							((Chronometer) findViewById(R.id.chronometer)).setBase(SystemClock.elapsedRealtime());
							((Chronometer) findViewById(R.id.chronometer)).start();
						
							String filePath = "Data File: " + collection.getDataFilePath();
							((EditText) findViewById(R.id.classificationText)).setText(filePath);
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
