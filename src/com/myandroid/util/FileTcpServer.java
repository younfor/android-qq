package com.myandroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.myandroid.activity.MainActivity;

import android.app.Activity;
import android.os.Message;

public class FileTcpServer {
	MainActivity mainA;

	public FileTcpServer(Activity mainA) {
		this.mainA = (MainActivity) mainA;
	}

	public void start() {
		server s = new server();
		s.start();
	}

	class server extends Thread {

		public void run() {
			try {
				creatServer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void creatServer() throws Exception {
		ServerSocket ss = new ServerSocket(2222);
		Socket s = new Socket();
		s = ss.accept();
		File file = new File(Tools.newsavepath + "/" + Tools.newfileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		BufferedInputStream is = new BufferedInputStream(s.getInputStream()); // ����
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));// д��
		Thread.sleep(1000);
		byte[] data = new byte[Tools.byteSize];// ÿ�ζ�ȡ���ֽ���
		int len= -1;
		while ((len=is.read(data) )!= -1) {
			os.write(data,0,len); 
			Tools.sendProgress+=len;//����
		}
		Tools.sendProgress=-1;	
		is.close();
		os.flush();
		os.close();
		s.close();
		Tools.Tips(Tools.SHOW, "�������:"+Tools.newfileName);
	}
}
