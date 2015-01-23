package com.myandroid.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
public class Media {
	// 录音文件播放
	private MediaPlayer myPlayer;
	// 录音
	private MediaRecorder myRecorder;
	// 音频文件保存地址
	public String sendpath,receivepath;// path/xx.amr就是完整路径
	public String name;//存储名字
	private File saveFilePath;
	public Media()
	{
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				sendpath = Environment.getExternalStorageDirectory()
						.getCanonicalPath().toString()
						+ "/MessageMediaSend";
				File files = new File(sendpath);
				if (!files.exists()) {
					files.mkdir();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				receivepath = Environment.getExternalStorageDirectory()
						.getCanonicalPath().toString()
						+ "/MessageMediaReceive";
				File files = new File(receivepath);
				if (!files.exists()) {
					files.mkdir();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//开始录制
	public void startRecord()
	{
		myRecorder = new MediaRecorder();
		// 从麦克风源进行录音
		myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		// 设置输出格式
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		// 设置编码格式
		myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		//保存路径
		this.name="AND"+Tools.getRandomId()
				+ new SimpleDateFormat(
						"yyyyMMddHHmmss").format(System
						.currentTimeMillis())
				+ ".amr";
		String paths = sendpath+"/"+name;
		saveFilePath = new File(paths);
		myRecorder.setOutputFile(saveFilePath
				.getAbsolutePath());
		try {
			saveFilePath.createNewFile();
			myRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 开始录音
		myRecorder.start();
	}
	//停止录制并保存
	public void stopRecord()
	{
		if (saveFilePath.exists() && saveFilePath != null) {
			myRecorder.stop();
			myRecorder.release();	
		}
	}
	//退出
	public void destroy()
	{
		// 释放资源
		if (myPlayer.isPlaying()) {
			myPlayer.stop();
			myPlayer.release();
		}
		myPlayer.release();
		myRecorder.release();
	}
	//开始播放
	public void startPlay(String path0)
	{
		myPlayer = new MediaPlayer();
		try {
			myPlayer.reset();
			myPlayer.setDataSource(path0);
			if (!myPlayer.isPlaying()) {

				myPlayer.prepare();
				myPlayer.start();
			} else {
				myPlayer.pause();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//停止播放
	public void stopPlay()
	{
		if (myPlayer.isPlaying()) {
			myPlayer.stop();
		}
	}
	
}
