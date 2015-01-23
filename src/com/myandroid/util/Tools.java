package com.myandroid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.conn.util.InetAddressUtils;

import com.myandroid.activity.ChatActivity;
import com.myandroid.activity.FileAcitivity;
import com.myandroid.activity.MainActivity;
import com.myandroid.message.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

public class Tools {
	// 协议命令
	public static final int CMD_ONLINE = 10;// 上线
	public static final int CMD_REPLYONLINE = 11;// 回应上线
	public static final int CMD_CHECK = 12;// 心跳广播
	public static final int CMD_SENDMSG=13;// 发送信息
	public static final int CMD_STARTTALK=14;// 发送呼叫请求
	public static final int CMD_STOPTALK=15;// 发送结束呼叫请求
	public static final int CMD_ACCEPTTALK=16;// 发送接收呼叫请求
	public static final int CMD_SENDMEDIA=17;//发送录音文件请求
	public static final int CMD_FINISHMEDIA=18;//完成录音文件接收
	public static final int CMD_PREPAREFILE=19;//确定请求录音文件
	public static final int CMD_FILEREQUEST=20;//请求传送文件
	public static final int CMD_FILEACCEPT=21;//接受文件请求
	public static final int CMD_FILEREFUSE=22;//拒绝文件请求
	public static final int CMD_UPDATEINFORMATION=23;//更新信息
	public static final int PORT_SEND=2426;// 发送端口
	public static final int PORT_RECEIVE=2425;// 接收端口
	public static final int PORT = 5760;//
	public static final int AUDIO_PORT = 5761;//语音端口
	// 消息命令
	public static boolean stoptalk=false;//语音开关
	public static final int MAINACTIVITY=7998;//当前是MAINACTIVITY
	public static final int CHATACTIVITY=7999;//CHATACTIVITY
	public static final int SHOW=8000;//显示消息
	public static final int FLUSH=8001;//刷新界面
	public static final int ADDUSER=8002;//添加用户
	public static final int DESTROYUSER=8003;//删除用户
	public static final int RECEIVEMSG=8004;//删除用户
	public static final int REFLESHCOUNT=8005;//更新计数
	public static final int ACTIVITY_MAIN=0;//mainA 构造函数专用
	public static final int ACTIVITY_CHART=1;//mainB
	public static MainActivity mainA=null;
	public static ChatActivity chart=null;
	public static int State=Tools.MAINACTIVITY;//状态，显示当前活跃activity
	public static String currentUserIp=null;
	public static Audio audio=new Audio();//语音监听线程
	public static Random random = new Random();
	public static String sign=":";
	public static String fileName=null;
	public static long fileSize=0;
	public static String currentPath=null;//当前录音文件路径
	//文件传送模块
	public static String newfileName=null;
	public static long newfileSize=0;
	public static final int ISFILE=1001;//是文件
	public static String startPath = FileAcitivity.ADDRESS;
	public static String newsavepath="/mnt/sdcard/myMes";
	public static int byteSize = 1024*5;// 每次读写文件的字节数
	public static double sendProgress = -1;// 每次读写文件的字节数s
	public static final int FILE_JINDU=2001;//进度命令
	public static final int PROGRESS_FLUSH=2002;//更新进度
	public static final int PROGRESS_COL=2003;//关闭进度条
	//消息缓存
	public static Map<String,List<Msg>> msgContainer = new HashMap<String,List<Msg>>();
	public static long pretime=0;
	public static User me=null;
	public static int[] headIconIds = {R.drawable.face0,
			R.drawable.face1,
			R.drawable.face2,
			R.drawable.face3,
			R.drawable.face4,
			R.drawable.face5,
			R.drawable.face6,
			R.drawable.face7,
			R.drawable.face8,
			R.drawable.face9,
			R.drawable.face10,
			R.drawable.face11,
			R.drawable.face12,
			R.drawable.face13,
			R.drawable.face14,
			R.drawable.face15};
	// 构造函数
	public Tools(Object o,int type){
		switch(type)
		{
			case Tools.ACTIVITY_MAIN:
				this.mainA=(MainActivity)o;
				break;
			case Tools.ACTIVITY_CHART:
				this.chart=(ChatActivity)o;
				break;
		}
		
	}
	// 发送消息
	public void sendMsg(Msg msg)
	{
		(new UdpSend(msg)).start();
	}
	// 发送消息线程
	class UdpSend extends Thread {
		Msg msg=null;
		UdpSend(Msg msg) {
			this.msg=msg;
		}

		public void run() {
			try {
				byte[] data = Tools.toByteArray(msg);
				DatagramSocket ds = new DatagramSocket(Tools.PORT_SEND);
				DatagramPacket packet = new DatagramPacket(data, data.length,
						InetAddress.getByName(msg.getReceiveUserIp()), Tools.PORT_RECEIVE);
				packet.setData(data);
				ds.send(packet);
				ds.close();
				//Tools.out("发送广播通知上线");
			} catch (Exception e) {
			}

		}
	}
	// 接收消息
	public void receiveMsg()
	{
		new UdpReceive().start();
	}
	// 接收消息线程
	class UdpReceive extends Thread {
		Msg msg=null;
		UdpReceive() {}

		public void run() {
			//消息循环
			while(true)
			{
				try {
					DatagramSocket ds = new DatagramSocket(Tools.PORT_RECEIVE);
					byte[] data = new byte[1024 * 4];
					DatagramPacket dp = new DatagramPacket(data, data.length);
					dp.setData(data);
					ds.receive(dp);
					byte[] data2 = new byte[dp.getLength()];
					System.arraycopy(data, 0, data2, 0, data2.length);// 得到接收的数据
					Msg msg = (Msg) Tools.toObject(data2);
					ds.close();
					//解析消息
					parse(msg);
				} catch (Exception e) {
				}
			}

		}
	}
	// 解析接收的
	public void parse(Msg msg)
	{
		switch (msg.getMsgType()) {
		
		case Tools.CMD_FILEACCEPT:
			//收到确认接受
			String path = Tools.chart.choosePath;
			Tools.TipsChat(Tools.SHOW, "正在发送文件:" + new File(path).getName());
			Tools.sendProgress=0;
			FileTcpClient tc0 = new FileTcpClient(msg, path);
			tc0.start();
			Tools.TipsChat(Tools.FILE_JINDU, "发送文件"+Tools.sign+"正在发送："+new File(path).getName()+Tools.sign+ (new File(path).length()));
			fileProgress();//启动进度条线程
			break;
		case Tools.CMD_FILEREFUSE:
			//收到拒绝接受
			Tools.TipsChat(Tools.SHOW, "对方拒绝接受文件");
			break;
		case Tools.CMD_FILEREQUEST:
			//收到传送文件请求
			Tools.out("收到文件传送请求");
			String[] newfileInfo = ((String) msg.getBody()).split(Tools.sign);
			Tools.newfileName = newfileInfo[0];// 记录下文件名称
			Tools.newfileSize = Long.parseLong(newfileInfo[1].trim());// 文件大小
			if(Tools.State==Tools.MAINACTIVITY)
			{
				Tools.out("正在main界面");
				Tools.Tips(Tools.CMD_FILEREQUEST, msg);
			}
			else if(Tools.State==Tools.CHATACTIVITY)
			{
				Tools.out("正在chat界面");
				Intent it = new Intent(Tools.chart, MainActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				Tools.chart.startActivity(it);
				Tools.Tips(Tools.CMD_FILEREQUEST, msg);
				//Tools.TipsChat(Tools.CMD_FILEREQUEST, msg);
			}
			break;
		case Tools.CMD_SENDMEDIA://对方发送录音文件
			if (!judgeUser(msg)) {// 如果列表无此人
				this.addUser(msg);// 列表添加此人
			}
			Tools.out("对方准备发送录音文件：");
			Tools.out(msg.getBody().toString());
			String[] fileInfo = (msg.getBody().toString()).split(Tools.sign);
			Tools.fileName = fileInfo[0];// 记录下文件名称
			
			Tools.fileSize = Long.parseLong(fileInfo[1].trim());// 文件大小
			
			Tools.out("名称:"+Tools.fileName+"大小:"+Tools.fileSize);
			// 接收文件 返回提示接受 建立tcp 服务器 接收文件
			MediaTcpServer ts = new MediaTcpServer(msg);
			ts.start();
			// 发送消息 让对方开始发送文件
			Msg msg0=new Msg(0,msg.getReceiveUser(), msg.getReceiveUserIp(), msg.getSendUser(), msg.getSendUserIp(),Tools.CMD_PREPAREFILE, null);
			sendMsg(msg0);
			break;
		case Tools.CMD_PREPAREFILE:
			//对方已经确定接收,开始发送录音文件
			Tools.out("开始发送录音文件");
			MediaTcpClient tc = new MediaTcpClient(msg, Tools.currentPath);
			tc.start();
			break;
		case Tools.CMD_ONLINE:// 上线
			upline(msg);
			break;
		case Tools.CMD_REPLYONLINE:// 响应上线
			replyUpline(msg);
			break;
		case Tools.CMD_CHECK:// 心跳接收
			updateHeart(msg);
			break;
		case Tools.CMD_SENDMSG:// 接收到对方发送的消息
			Tools.out(msg.getSendUser()+"来消息了");
			receiveMsg(msg);
			break;
		case Tools.CMD_STARTTALK: // 语音请求
			Tools.out("来自"+msg.getSendUserIp()+"语音请求");
			Tools.stoptalk=false;
			if(Tools.State==Tools.MAINACTIVITY)
			{
				Tools.out("正在main界面");
				Tips(Tools.CMD_STARTTALK,msg);
			}
			else if(Tools.State==Tools.CHATACTIVITY)
			{
				Tools.out("正在chat界面");
				TipsChat(Tools.CMD_STARTTALK,msg);
			}
			break;
		case Tools.CMD_STOPTALK:// 关闭请求
			Tools.stoptalk=true;
			if(Tools.State==Tools.MAINACTIVITY)
			{
				Tools.out("正在main界面");
				Tips(Tools.CMD_STOPTALK,msg);
			}
			else if(Tools.State==Tools.CHATACTIVITY)
			{
				Tools.out("正在chat界面");
				TipsChat(Tools.CMD_STOPTALK,msg);
			}
			break;
		case Tools.CMD_ACCEPTTALK: // 被叫应答
			//开始通话
			if(!Tools.stoptalk){
				User person=new User(msg.getSendUser(),msg.getSendUserIp() );
				Tools.audio.audioSend(person);
			}
			break;
		}
	}
	// 接收消息
	public void receiveMsg(Msg msg)
	{
		Tips(Tools.SHOW,msg.getSendUser() + " 发来消息！");
		if (!judgeUser(msg)) {// 如果列表无此人
			this.addUser(msg);// 列表添加此人
		}
		//如果当前界面是MainActivity
		if(Tools.State==Tools.MAINACTIVITY)
		{
			Tools.out("mainacitivity界面");
			// 存储缓存消息，并提示条数
			List<Msg> mes=null;
			if(msgContainer.containsKey(msg.getSendUserIp()))
			{// 如果存在此人的消息缓存
				mes=msgContainer.get(msg.getSendUserIp());
				Tools.out("存在缓存");
			}else
			{
				mes=new ArrayList<Msg>();
				Tools.out("不存在");
			}
			// 加入缓存
			mes.add(msg);
			msgContainer.put(msg.getSendUserIp(), mes);
			Tools.out("更新计数");
			Tips(Tools.REFLESHCOUNT,msg.getSendUserIp());
		}
		//如果当前界面是ChartActivity
		if(Tools.State==Tools.CHATACTIVITY)
		{
			Tools.out("chart界面");
			TipsChat(Tools.RECEIVEMSG, msg);
		}
	}
	// 接收心跳广播
	public void updateHeart(Msg msg)
	{
		for (int i = 0; i < mainA.userList.size(); i++) 
		{
			if (mainA.userList.get(i).getIp().equals(msg.getSendUserIp())) 
			{
				mainA.userList.get(i).setOnlineTime(System.currentTimeMillis());
			}
		}
	}
	// 接收到上线广播
	public void upline(Msg msg){
		if (!judgeUser(msg)) {// 如果不存在
			//Tips(Tools.SHOW,msg.getSendUser() + " 上线···");
			addUser(msg);// 添加此人
		}
		// 发送响应上线
		Msg msgsend=new Msg();
		msgsend.setSendUser(Tools.me.getName());
		msgsend.setSendUserIp(Tools.me.getIp());
		msgsend.setHeadIconPos(Tools.me.getHeadIconPos());
		msgsend.setMsgType(Tools.CMD_REPLYONLINE);
		msgsend.setReceiveUserIp(msg.getSendUserIp());
		msgsend.setPackId(Tools.getTimel());
		Tools.out(Tools.me.getIp()+"回复广播"+msg.getSendUserIp());
		// 发送消息
		sendMsg(msgsend);
	}
	// 判断是否有此人 更新
	public boolean judgeUser(Msg msg) {// false 表示不存在
		for (int i = 0; i < mainA.userList.size(); i++) 
		{
			if (mainA.userList.get(i).getIp().equals(msg.getSendUserIp())) 
			{
				// 如果存在 改名字
				if (!mainA.userList.get(i).getName().equals(msg.getSendUser()))
				{
					mainA.userList.get(i).setName(msg.getSendUser());// 该在线列表的名字
					mainA.adapterList.get(i).put("name", msg.getSendUser());
					//刷新列表													
					Tips(Tools.FLUSH,null);
				}
				if (mainA.userList.get(i).getHeadIconPos()!=msg.getHeadIconPos())
				{
					mainA.userList.get(i).setHeadIconPos(msg.getHeadIconPos());// 该在线列表的名字
					mainA.adapterList.get(i).put("headicon", Tools.headIconIds[msg.getHeadIconPos()]);
					//刷新列表													
					Tips(Tools.FLUSH,null);
				}
				return true;
			}
		}
		return false;
	}
	// 添加在线用户
	public void addUser(Msg msg) {
		User user = new User(msg.getSendUser(), msg.getSendUserIp(), 0,System.currentTimeMillis());
		// 在线列表加人
		mainA.userList.add(user);
		// 为其创建聊天记录
		// Tools.MsgEx.put(msg.getSendUserIp(), "");
		// listView加人
		Map map = new HashMap<String, String>();
		map.put("name", user.getName());
		map.put("ip", "IP:"+user.getIp());
		map.put("UnReadMsgCount", "");
		map.put("headicon", Tools.headIconIds[user.getHeadIconPos()]);
		// 刷新列表
		Tips(Tools.ADDUSER,map);
	}
	// 接收响应上线
	public void replyUpline(Msg msg){
		Tools.out("接受响应上线"+msg.getSendUserIp());
		if (!judgeUser(msg)) {// 如果不存在
			Tips(Tools.SHOW,msg.getSendUser() + " 上线···");
			addUser(msg);// 添加此人
		}
	}
	// 获取当前时间
	public static long getTimel() {
		return (new Date()).getTime();
	}
	// 得到广播ip, 192.168.0.255之类的格式
	public static String getBroadCastIP() {
		String ip = getLocalHostIp().substring(0,
				getLocalHostIp().lastIndexOf(".") + 1)
				+ "255";
		return ip;
	}
	// 获取本机IP
	public static String getLocalHostIp() {
		String ipaddress = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			// 遍历所用的网络接口
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 遍历每一个接口绑定的所有ip
				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ip
									.getHostAddress())) {
						return ipaddress = ip.getHostAddress();
					}
				}

			}
		} catch (SocketException e) {
			System.out.print("获取IP失败");
			e.printStackTrace();
		}
		return ipaddress;

	}
	// 对象封装成消息
	public static byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}
	// 消息解析成对象
	public static Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}
	// 时间转换
	public static String getChangeTime(long timel) {
		//yyyy-MM-dd HH:mm:ss
		if(System.currentTimeMillis()-Tools.pretime<60000)
		{
			Tools.pretime=timel;
			return null;
		}else
		{
			Tools.pretime=timel;
			SimpleDateFormat sfd = new SimpleDateFormat("MM月dd日 HH点mm分");
			return sfd.format(timel);
		}
	}
	// Tips-Handler
	public static void Tips(int cmd,Object str) {
		Message m = new Message();
		m.what = cmd;
		m.obj = str;
		mainA.handler.sendMessage(m);
	}
	public static void TipsChat(int cmd,Object str)
	{
		Message m = new Message();
		m.what = cmd;
		m.obj = str;
		Tools.chart.handler.sendMessage(m);
	}
	public static void out(String s)
	{
		Log.v("mes", s);
	}
	// 开启心跳检查
	public void startCheck()
	{
		new HeartBroadCast().start();
		new CheckUserOnline().start();
	}
	// 心跳响应广播
	class HeartBroadCast extends Thread{
		public void run()
		{
			while(!mainA.isPaused)
			{
				try {
					sleep(10000);
				
				} catch (InterruptedException e) {
				}
				Msg msgBroad=new Msg();
				msgBroad.setSendUser(Tools.me.getName());
				msgBroad.setSendUserIp(Tools.me.getIp());
				msgBroad.setMsgType(Tools.CMD_CHECK);
				msgBroad.setReceiveUserIp(Tools.getBroadCastIP());
				msgBroad.setPackId(Tools.getTimel());
				// 发送消息
				sendMsg(msgBroad);
			}
		}
	}
	// 检测用户是否在线，如果超过15说明用户已离线，秒则从列表中清除该用户
	class CheckUserOnline extends Thread{
		@Override
		public void run()
		{
			while(!mainA.isPaused)
			{
				for (int i = 0; i < mainA.userList.size(); i++) 
				{
					long cm=System.currentTimeMillis()-mainA.userList.get(i).getOnlineTime();
					
					if(cm>15000)
					{
						//刷新列表													
						Tips(Tools.DESTROYUSER,i);
						Tools.out("刷新");
					}
				}
				try {
					sleep(8000);
					//防掉线，广播
					//Tips(Tools.CONSTANTBROAD,null);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	// 语音
	//startTalk isStopTalk = false;
	//acceptTalk
	//stopTalk isStopTalk = true;
	// 取得随机数
	public static String getRandomId()
	{
		return random.nextInt(9999)+"";
	}
	public void fileProgress() {
		new Thread() { 
			public void run() {
				
				while (Tools.sendProgress != -1) {
					 Message m = new Message();
						m.what = Tools.PROGRESS_FLUSH;
					Tools.chart.handler.sendMessage(m);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 关闭进度条
				Message m1 = new Message();
				m1.what = Tools.PROGRESS_COL;
				Tools.chart.handler.sendMessage(m1);
			}
		}.start();
	}
}
