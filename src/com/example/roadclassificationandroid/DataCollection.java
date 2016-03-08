package com.example.roadclassificationandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.EditText;

public class DataCollection implements SensorEventListener {

	// Audio Constants
	private static int[] mSampleRates = { 44100, 22050, 11025, 8000 };
	private int AUDIO_FORMAT = 0;
	private int AUDIO_SOURCE = 0;
	private int BUFFER_SIZE = 0;
	private int CHANNEL_IN_CONFIG = 0;
	private int SAMPLING_RATE = 0;

	// Miscellaneous Audio Variables
	private String audioFilePath = "";
	private Thread audioThread = null;
	private AudioRecord audiorec = null;
	private boolean isRecording = false;

	// Sensor variables
	private final SensorManager sensorManager;
	private Sensor gyroscope = null;
	private Sensor accelerom = null;

	// Timing Variables
	private boolean isStarted = false;
	private long startTime = 0L;

	// File Handling
	private BufferedWriter sensorbw = null;
	private BufferedWriter audiobw = null;
	private BufferedWriter databw = null;

	private File dcimDir = null;
	private String classification = "";
	private String classificationDirPath = "";
	private String sensorFilePath = "";
	private String dataFilePath = "";

	private Activity currentActivity;

	public DataCollection(Activity paramActivity) {
		currentActivity = paramActivity;
		sensorManager = (SensorManager) currentActivity.getSystemService(Context.SENSOR_SERVICE);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		accelerom = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		dcimDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	}

	private AudioRecord getAudioRecorder() {
		try {
			AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT; // 0
			SAMPLING_RATE = mSampleRates[0]; // 44100
			CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO; // 16
			AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 2
			BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT);
			AudioRecord localAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
			return localAudioRecord;
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return null;
	}

	private void sampleAudioData() {
		int numShorts = BUFFER_SIZE / 2;
		short[] audioArray = new short[numShorts];
		try {
			if (!isRecording) {
				return;
			}
			long endTime = System.nanoTime();
			long tempStartTime = startTime;
			audiorec.read(audioArray, 0, numShorts);

			int audioArrayLength = audioArray.length;
			for (int i = 0; i < audioArrayLength; i++) {
				int audioReading = audioArray[i];
				audiobw.write("audio,"
						+ String.valueOf(endTime - tempStartTime) + ","
						+ String.valueOf(audioReading));
				audiobw.newLine();
				audiobw.flush();
			}
			return;
		} catch (IOException e) {
			System.err.println("Cannot Write Audio Data File.");
		}
	}

	public boolean StartCollection() {
		boolean success = true;

		if (!isStarted) {
			classification = ((EditText) currentActivity
					.findViewById(R.id.classificationText)).getText()
					.toString();
			classificationDirPath = (dcimDir.getAbsolutePath() + "/Classification/");
			(new File(classificationDirPath)).mkdirs();

			int i = 1;
			sensorFilePath = (classificationDirPath + classification + "_0_sensor.csv");
			audioFilePath = (classificationDirPath + classification + "_0_audio.csv");
			dataFilePath = (classificationDirPath + classification + "_0.csv");
			while (!(new File(sensorFilePath).exists())
					|| !(new File(audioFilePath).exists())
					|| !(new File(dataFilePath).exists())) {
				sensorFilePath = (classificationDirPath + classification + "_"
						+ i + "_sensor.csv");
				audioFilePath = (classificationDirPath + classification + "_"
						+ i + "_audio.csv");
				dataFilePath = (classificationDirPath + classification + "_"
						+ i + ".csv");
				i++;
				if (i == 100) {
					success = false;
					break; // Escape just in case file path is incorrect. don't
							// run in infinite loop.
				}
			}

			System.out.println("Sensor Path " + sensorFilePath);
			System.out.println("Audio Path " + sensorFilePath);
			System.out.println("Data Path " + dataFilePath);

			if (success) {
				try {
					sensorbw = new BufferedWriter(new FileWriter(
							sensorFilePath, true));
					audiobw = new BufferedWriter(new FileWriter(audioFilePath,
							true));
					databw = new BufferedWriter(new FileWriter(dataFilePath,
							true));
				} catch (IOException e) {
					System.err.println("Could not write to data file.");
					success = false;
				}
			}

			startTime = System.nanoTime();

			if (success) {
				success = sensorManager.registerListener(this, gyroscope,
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (success) {
				success = sensorManager.registerListener(this, accelerom,
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (success) {
				audiorec = getAudioRecorder();
				if (audiorec != null) {
					audiorec.startRecording();
					isRecording = true;
					audioThread = new Thread(new Runnable() {
						public void run() {
							sampleAudioData();
						}
					}, "AudioRecorder Thread");
					audioThread.start();
					isStarted = true;
				} else {
					System.out.println("Could not find audio recorder.");
					success = false;
				}
			}
		}
		return success;
	}

	public boolean StopCollection() {
		boolean success = false;
		if (isStarted) {
			if (gyroscope != null) {
				sensorManager.unregisterListener(this, gyroscope);
			}
			if (accelerom != null) {
				sensorManager.unregisterListener(this, accelerom);
			}
			if (audiorec != null) {
				isRecording = false;
				audiorec.stop();
				audiorec.release();
			}
			try {
				audioThread.join();
				audioThread = null;
			} catch (InterruptedException e) {
				System.err.println("Could not join audio thread.");
			}

			try {
				BufferedReader sensorbr = new BufferedReader(
						new InputStreamReader(new FileInputStream(
								sensorFilePath)));
				BufferedReader audiobr = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(audioFilePath)));
				databw.write(classification);
				databw.newLine();
				databw.flush();

				String line = sensorbr.readLine();
				while (line != null) {
					databw.write(line);
					databw.newLine();
					databw.flush();

					line = sensorbr.readLine();
				}

				line = audiobr.readLine();
				while (line != null) {
					databw.write(line);
					databw.newLine();
					databw.flush();

					line = audiobr.readLine();
				}

				sensorbr.close();
				audiobr.close();
				if ((new File(dataFilePath)).isFile()) {
					(new File(sensorFilePath)).delete();
					(new File(audioFilePath)).delete();
				}

				success = true;
			} catch (IOException e) {
				try {
					sensorbw.close();
					audiobw.close();
					databw.close();
					isStarted = false;
					System.err.println("Cannot write data file.");
				} catch (IOException e2) {
				}
			}
		}

		return success;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}

	public void onAccuracyChanged(Sensor paramSensor, int paramInt) {
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		String sensor = sensorEvent.sensor.toString();
		long elapsedTime = System.nanoTime() - startTime;
		try {
			sensorbw.write(sensor.substring(sensor.lastIndexOf(".") + 1)
					+ "_x," + String.valueOf(elapsedTime) + ","
					+ String.valueOf(sensorEvent.values[0]));
			sensorbw.newLine();
			sensorbw.write(sensor.substring(sensor.lastIndexOf(".") + 1)
					+ "_y," + String.valueOf(elapsedTime) + ","
					+ String.valueOf(sensorEvent.values[1]));
			sensorbw.newLine();
			sensorbw.write(sensor.substring(sensor.lastIndexOf(".") + 1)
					+ "_z," + String.valueOf(elapsedTime) + ","
					+ String.valueOf(sensorEvent.values[2]));
			sensorbw.newLine();
			sensorbw.flush();
			return;
		} catch (IOException e) {
			System.err.println("Cannot Write Sensor Data File.");
		}
	}
}
