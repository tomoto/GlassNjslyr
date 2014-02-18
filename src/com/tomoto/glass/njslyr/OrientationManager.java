package com.tomoto.glass.njslyr;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationManager {
	private SensorManager sensorManager;
	private Sensor rotationSensor;
	private Listener listener;
	private boolean started;
	
	private float[] rotationMatrix;
	private float[] orientation;
	
	public interface Listener {
		void onSensorChanged(OrientationManager manager);
	}
	
	private SensorEventListener rotationSensorEventListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, rotationMatrix);
            SensorManager.getOrientation(rotationMatrix, orientation);
            fireSensorChanged();
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};

	public OrientationManager(SensorManager sensorManager, Listener listener) {
		this.sensorManager = sensorManager;
		this.rotationSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		this.listener = listener;
		
		rotationMatrix = new float[16];
		orientation = new float[9];
	}
	
	protected void fireSensorChanged() {
        listener.onSensorChanged(this);
	}

	public void start() {
		sensorManager.registerListener(rotationSensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_UI);
		started = true;
	}
	
	public void stop() {
		sensorManager.unregisterListener(rotationSensorEventListener);
		started = false;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public float getAzimuth() {
		return (float) Math.toDegrees(orientation[0]);
	}
	
	public float getPitch() {
		return (float) Math.toDegrees(orientation[1]);
	}
	
	public float getRoll() {
		return (float) Math.toDegrees(orientation[2]);
	}
	
}
