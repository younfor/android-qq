package com.myandroid.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.myandroid.activity.MainActivity;

import android.app.Activity;
import android.os.Message;
public class MediaTcpServer {
	Msg msg=null;
	public MediaTcpServer(Msg msg){
		this.msg=msg;
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
				e.printStackTrace();
			}
		}
	}
	public void creatServer() throws Exception {
		ServerSocket ss = new ServerSocket(2222);
		Socket s = new Socket();
		s = ss.accept();
		File file = new File(new Media().receivepath + "/" + Tools.fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		BufferedInputStream is = new BufferedInputStream(s.getInputStream()); // 读进
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));// 写出
		Thread.sleep(1000);
		//int n = 1;
		//long part = Tools.fileSize / 1024*5;// 分成几段
		//long surplus = Tools.fileSize % 1024*5;// 最后剩余多少段
		byte[] data = new byte[1024*5];// 每次读取的字节数
		int len= -1;
		while ((len=is.read(data) )!= -1) {
			os.write(data,0,len); 
			//Tools.sendProgress+=len;//进度
		}
		//Tools.sendProgress=-1;	
		is.close();
		os.flush();
		os.close();
		s.close();
		//接收完成
		//可以开始显示录音view到聊天窗口了，用handler告诉聊天窗口
		if(Tools.State==Tools.MAINACTIVITY)
		{
			Tools.out("正在main界面");//还没写
			// 存储缓存消息，并提示条数
			List<Msg> mes=null;
			if(Tools.msgContainer.containsKey(msg.getSendUserIp()))
			{// 如果存在此人的消息缓存
				mes=Tools.msgContainer.get(msg.getSendUserIp());
				Tools.out("存在缓存");
			}else
			{
				mes=new ArrayList<Msg>();
				Tools.out("不存在");
			}
			// 加入缓存
			msg.setMsgType(Tools.ISFILE);
			mes.add(msg);
			Tools.msgContainer.put(msg.getSendUserIp(), mes);
			Tools.out("更新计数");
			Tools.Tips(Tools.CMD_FINISHMEDIA,msg);
		}
		else if(Tools.State==Tools.CHATACTIVITY)
		{
			Tools.out("正在chat界面接收录音");
			Tools.TipsChat(Tools.CMD_FINISHMEDIA,msg);
		}
		//tiShi("接收文成：" + Tools.fileName);
	}

	
}
