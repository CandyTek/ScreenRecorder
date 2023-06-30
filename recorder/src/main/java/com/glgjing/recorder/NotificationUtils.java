package com.glgjing.recorder;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * 通知工具类
 *
 * @see <a href="https://www.jianshu.com/p/839672499aaa">Android适配总结之通知</a>
 */
public final class NotificationUtils {
	/**
	 * 聊天消息渠道
	 */
	public static final String  CHANNEL_ID_CHAT = "chat";
	/**
	 * 咨询消息渠道
	 */
	public static final String  CHANNEL_ID_NEWS = "news";
	/**
	 * 报警消息渠道
	 */
	public static final String  CHANNEL_ID_WARNING = "warning";

	/**
	 * chat通知id
	 */
	public static final int NOTIFICATION_ID_CHAT = 1;
	/**
	 * 报警通知id
	 */
	public static final int NOTIFICATION_ID_WARNING = 2;
	/**
	 * 咨询通知id
	 */
	public static final int NOTIFICATION_ID_NEWS = 3;

	/**
	 * 初始化通知渠道，可以在 {@link android.app.Application#onCreate()} 方法里调用
	 */
	public static void initNotification(Application application){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			// 聊天消息
			createNotificationChannel(application,CHANNEL_ID_CHAT,"聊天消息通知",NotificationManager.IMPORTANCE_LOW);
			createNotificationChannel(application,CHANNEL_ID_NEWS,"系统消息通知",NotificationManager.IMPORTANCE_HIGH);
			createNotificationChannel(application,CHANNEL_ID_WARNING,"报警消息通知",NotificationManager.IMPORTANCE_HIGH);
		}
	}

	/**
	 * 创建通知渠道
	 * @param channelId 通知渠道ID，Android O 以后的通知改版
	 * @param channelName 通知渠道名
	 * @param importance 通知重要程度，用于该通知是否需处于通知栏靠前的位置
	 */
	@TargetApi(Build.VERSION_CODES.O)
	private static void createNotificationChannel(Application application,String channelId, String channelName, int importance){
		NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
		NotificationManager notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.createNotificationChannel(channel);
	}

}

