package com.glgjing.recorder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service {
	private MediaProjection mediaProjection;
	private MediaRecorder mediaRecorder;
	private VirtualDisplay virtualDisplay;

	private boolean running;
	private int width = 720;
	private int height = 1080;
	private int dpi;

	@Override
	public IBinder onBind(Intent intent) {
		return new RecordBinder();
	}

	@Override
	public int onStartCommand(Intent intent,int flags,int startId) {
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread serviceThread = new HandlerThread("service_thread",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		serviceThread.start();
		running = false;
		mediaRecorder = new MediaRecorder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setMediaProject(MediaProjection project) {
		mediaProjection = project;
	}

	public boolean isRunning() {
		return running;
	}

	public void setConfig(int width,int height,int dpi) {
		this.width = width;
		this.height = height;
		this.dpi = dpi;
	}

	public boolean startRecord() {
		if (mediaProjection == null || running) {
			return false;
		}

		initRecorder();
		createVirtualDisplay();
		mediaRecorder.start();
		running = true;
		return true;
	}

	public boolean stopRecord() {
		if (!running) {
			return false;
		}
		running = false;
		mediaRecorder.stop();
		mediaRecorder.reset();
		virtualDisplay.release();
		mediaProjection.stop();

		return true;
	}

	private void createVirtualDisplay() {
		virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen",width,height,dpi,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mediaRecorder.getSurface(),null,null);
	}

	private void initRecorder() {
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mediaRecorder.setOutputFile(getsaveDirectory() + System.currentTimeMillis() + ".mp4");
		mediaRecorder.setVideoSize(width,height);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
		mediaRecorder.setVideoFrameRate(30);
		try {
			mediaRecorder.prepare();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getsaveDirectory() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

			File file = new File(rootDir);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					return null;
				}
			}

			Toast.makeText(getApplicationContext(),rootDir,Toast.LENGTH_SHORT).show();

			return rootDir;
		} else {
			return null;
		}
	}

	public class RecordBinder extends Binder {
		public RecordService getRecordService() {
			return RecordService.this;
		}
	}

	/** 创建录屏通知，大于api 26的录屏所使用 */
	public void createNotification() {
		if (Build.VERSION.SDK_INT >= 26) {
			PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
			Notification.Builder notificationBuilder = (new Notification.Builder(this,NotificationUtils.CHANNEL_ID_WARNING))
					// .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),))
					// .setTicker("test")
					.setSmallIcon(R.drawable.baseline_videocam_24)
					.setContentTitle("正在录制屏幕...")
					.setContentText("点击停止")
					.setContentIntent(pendingIntent);
			this.startForeground(1,notificationBuilder.build());
		}
	}

	/** 取消录屏通知 */
	public void cancleNotification(){
		if (Build.VERSION.SDK_INT >= 26) {
			this.stopForeground(true);
		}
	}
}
