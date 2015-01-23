package com.myandroid.util;

import java.io.Serializable;

public class Msg implements Serializable {
	private final String edition = "version0";// �汾��
	private long packId;//ʱ���
	private String sendUser;// ������ �û���
	private String sendUserIp;// ������ip
	private String receiveUser;// ������
	private String receiveUserIp;//������IP
	private int headIconPos;//ͷ��
	private int MsgType;// ��Ϣ����
	private Object body;// ����
	
	public int getHeadIconPos() {
		return headIconPos;
	}
	public void setHeadIconPos(int headIconPos) {
		this.headIconPos = headIconPos;
	}
	public Msg (){}
	public Msg(int headIconPos,String sendUser, String sendUserIp, String receiveUser,
			String receiveIp, int msgType, Object body) {
		super();
		this.headIconPos=headIconPos;
		this.sendUser = sendUser;
		this.sendUserIp = sendUserIp;
		this.receiveUser = receiveUser;
		this.receiveUserIp = receiveIp;
		MsgType = msgType;
		this.body = body;
	}
	
	public long getPackId() {
		return packId;
	}
	public void setPackId(long packId) {
		this.packId = packId;
	}
	public String getReceiveUserIp() {
		return receiveUserIp;
	}
	public void setReceiveUserIp(String receiveUserIp) {
		this.receiveUserIp = receiveUserIp;
	}
	public String getSendUser() {
		return sendUser;
	}
	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}
	public String getSendUserIp() {
		return sendUserIp;
	}
	public void setSendUserIp(String sendUserIp) {
		this.sendUserIp = sendUserIp;
	}
	public String getReceiveUser() {
		return receiveUser;
	}
	public void setReceiveUser(String receiveUser) {
		this.receiveUser = receiveUser;
	}
	public int getMsgType() {
		return MsgType;
	}
	public void setMsgType(int msgType) {
		MsgType = msgType;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
	public String getEdition() {
		return edition;
	}



	
}
