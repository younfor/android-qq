package com.myandroid.util;

public class User implements java.io.Serializable {

	private String name="";

	private String ip;
	private long OnlineTime; 
	private int unReadMsgCount = 0;
	private int headIconPos=0;

	public int getHeadIconPos() {
		return headIconPos;
	}

	public void setHeadIconPos(int headIconPos) {
		this.headIconPos = headIconPos;
	}


	public long getOnlineTime() {
		return OnlineTime;
	}

	public void setOnlineTime(long onlineTime) {
		OnlineTime = onlineTime;
	}

	public int getUnReadMsgCount() {
		return unReadMsgCount;
	}

	public void setUnReadMsgCount(int unReadMsgCount) {
		this.unReadMsgCount = unReadMsgCount;
	}

	public User(String name, String ip) {
		super();
		this.name = name;

		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public User(String name, String ip, int unReadMsgCount,long time) {
		super();
		this.name = name;
		this.ip = ip;
		this.unReadMsgCount = unReadMsgCount;
		this.OnlineTime=time;
	}
	public User getCopy()
	{
		User u=new User(this.name,this.ip,this.unReadMsgCount,this.OnlineTime);
	
		u.setHeadIconPos(this.headIconPos);
		return u;
	}

}
