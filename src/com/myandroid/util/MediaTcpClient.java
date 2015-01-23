package com.myandroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class MediaTcpClient {
	Msg msg = null;
	String path = null;

	public MediaTcpClient(Msg msg, String path) {
		this.msg = msg;
		this.path = path;
	}

	public void start() {
		Client c = new Client();
		c.start();
	}

	class Client extends Thread {

		public void run() {
			try {
				creatClient();
				// Tools.sendProgress=-1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void creatClient() throws Exception {
		Socket s = new Socket(msg.getSendUserIp(), 2222);
		// 读文件
		File file = new File(path);
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream os =new BufferedOutputStream( s.getOutputStream());
		// 读文件
		double n = 1;
//		long part = file.length() / Tools.byteSize;// 分成几段
//		long surplus = file.length() % Tools.byteSize;// 最后剩余多少段
		byte[] data = new byte[1024*5];// 每次读取的字节数
		int len=-1;
		while ((len=is.read(data))!= -1) {
			os.write(data,0,len); 
			//Tools.sendProgress+=len;//进度
		}
		//Tools.sendProgress=-1;
		is.close();
		os.flush();
		os.close();
		//发送消息告诉几经上传完
		Tools.out("发送完成");
	}
}
