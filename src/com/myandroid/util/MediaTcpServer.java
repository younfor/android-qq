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
		BufferedInputStream is = new BufferedInputStream(s.getInputStream()); // ����
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));// д��
		Thread.sleep(1000);
		//int n = 1;
		//long part = Tools.fileSize / 1024*5;// �ֳɼ���
		//long surplus = Tools.fileSize % 1024*5;// ���ʣ����ٶ�
		byte[] data = new byte[1024*5];// ÿ�ζ�ȡ���ֽ���
		int len= -1;
		while ((len=is.read(data) )!= -1) {
			os.write(data,0,len); 
			//Tools.sendProgress+=len;//����
		}
		//Tools.sendProgress=-1;	
		is.close();
		os.flush();
		os.close();
		s.close();
		//�������
		//���Կ�ʼ��ʾ¼��view�����촰���ˣ���handler�������촰��
		if(Tools.State==Tools.MAINACTIVITY)
		{
			Tools.out("����main����");//��ûд
			// �洢������Ϣ������ʾ����
			List<Msg> mes=null;
			if(Tools.msgContainer.containsKey(msg.getSendUserIp()))
			{// ������ڴ��˵���Ϣ����
				mes=Tools.msgContainer.get(msg.getSendUserIp());
				Tools.out("���ڻ���");
			}else
			{
				mes=new ArrayList<Msg>();
				Tools.out("������");
			}
			// ���뻺��
			msg.setMsgType(Tools.ISFILE);
			mes.add(msg);
			Tools.msgContainer.put(msg.getSendUserIp(), mes);
			Tools.out("���¼���");
			Tools.Tips(Tools.CMD_FINISHMEDIA,msg);
		}
		else if(Tools.State==Tools.CHATACTIVITY)
		{
			Tools.out("����chat�������¼��");
			Tools.TipsChat(Tools.CMD_FINISHMEDIA,msg);
		}
		//tiShi("�����ĳɣ�" + Tools.fileName);
	}

	
}
