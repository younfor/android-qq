package com.myandroid.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
public class Media {
	// ¼���ļ�����
	private MediaPlayer myPlayer;
	// ¼��
	private MediaRecorder myRecorder;
	// ��Ƶ�ļ������ַ
	public String sendpath,receivepath;// path/xx.amr��������·��
	public String name;//�洢����
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
	//��ʼ¼��
	public void startRecord()
	{
		myRecorder = new MediaRecorder();
		// ����˷�Դ����¼��
		myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		// ���������ʽ
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		// ���ñ����ʽ
		myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		//����·��
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
		// ��ʼ¼��
		myRecorder.start();
	}
	//ֹͣ¼�Ʋ�����
	public void stopRecord()
	{
		if (saveFilePath.exists() && saveFilePath != null) {
			myRecorder.stop();
			myRecorder.release();	
		}
	}
	//�˳�
	public void destroy()
	{
		// �ͷ���Դ
		if (myPlayer.isPlaying()) {
			myPlayer.stop();
			myPlayer.release();
		}
		myPlayer.release();
		myRecorder.release();
	}
	//��ʼ����
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
	//ֹͣ����
	public void stopPlay()
	{
		if (myPlayer.isPlaying()) {
			myPlayer.stop();
		}
	}
	
}
