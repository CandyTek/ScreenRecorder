package com.glgjing.recorder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
	private static final int RECORD_REQUEST_CODE = 101;
	private static final String[] NEEDED_PERMISSIONS = new String[]{
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.RECORD_AUDIO
	};

	private Button startBtn;

	private MediaProjectionManager projectionManager;
	private MediaProjection mediaProjection;
	private RecordService recordService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
		setContentView(R.layout.activity_main);

		startBtn = (Button) findViewById(R.id.start_record);
		startBtn.setEnabled(false);
		startBtn.setOnClickListener(v -> {
			if (recordService.isRunning()) {
				recordService.stopRecord();
				recordService.cancleNotification();
				startBtn.setText(R.string.start_record);
			} else {
				Intent captureIntent = projectionManager.createScreenCaptureIntent();
				startActivityForResult(captureIntent,RECORD_REQUEST_CODE);
			}
		});

		if (!checkPermission(this,NEEDED_PERMISSIONS)) {
			ActivityCompat.requestPermissions(this,NEEDED_PERMISSIONS,1);
		}

		Intent intent = new Intent(this,RecordService.class);
		bindService(intent,connection,BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
		if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
			recordService.createNotification();
			mediaProjection = projectionManager.getMediaProjection(resultCode,data);
			recordService.setMediaProject(mediaProjection);
			recordService.startRecord();
			startBtn.setText(R.string.stop_record);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
		boolean isAllGranted = true;
		for (int grantResult : grantResults) {
			isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
		}
		// 权限没有全部获取，退出
		if (!isAllGranted) {
			finish();
		}
	}

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,IBinder service) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
			recordService = binder.getRecordService();
			recordService.setConfig(metrics.widthPixels,metrics.heightPixels,metrics.densityDpi);
			startBtn.setEnabled(true);
			startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {}
	};

	// 是否拥有权限
	public static boolean checkPermission(Context context,String[] permissions) {
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED)
				return false;
		}
		return true;
	}

}
