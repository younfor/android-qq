package com.myandroid.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myandroid.message.R;
import com.myandroid.util.Audio;
import com.myandroid.util.FileTcpServer;
import com.myandroid.util.Msg;
import com.myandroid.util.Tools;
import com.myandroid.util.User;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	//参数
	AlertDialog revTalkDialog=null;
	public boolean isPaused=false;
	ListView listView=null;
	public List<User> userList = null;
	public List<Map<String, Object>> adapterList = null;
	Tools tools=null;
	SimpleAdapter adapter=null;
	Msg m=null;
	ProgressDialog proDia = null;
	Double fileSize=0.0;
	TextView titlename=null;
	SettingDialog settingDialog=null;
	ImageView myicon=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_main);
		//初始化布局
		Tools.State=Tools.MAINACTIVITY;//状态
		Tools.mainA=this;
		init();
		tools=new Tools(this,Tools.ACTIVITY_MAIN);
		//广播上线(包括自己)
		reBroad();
		Tools.out("开始");
		// 开启接收端 时时更新在线列表
		tools.receiveMsg();
		// 心跳
		tools.startCheck();
		// 语音监听
		Tools.audio.start();
	}
	// 初始化布局
	public void init()
	{
		myicon=(ImageView)findViewById(R.id.friend_list_myImg);
		listView = (ListView) super.findViewById(R.id.listView);
		// 列表项监听
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//转入chatActivity
				Intent it = new Intent(MainActivity.this, ChatActivity.class);
				it.putExtra("person", userList.get(arg2));
				MainActivity.this.startActivityForResult(it, 1);
				// 当前人的聊天提示信息清零
				for (int i = 0; i < adapterList.size(); i++) {
					if (adapterList.get(i).get("ip").equals("IP:"+userList.get(arg2).getIp())) { // 遍历
						adapterList.get(i).put("UnReadMsgCount", "");
						tools.Tips(Tools.FLUSH, null);
						Tools.out("清零");
						Tools.out("转入chart");
					}
				}
			}
		});
		// 初始化自己
		userList = new ArrayList<User>();
		Tools.me = new User(Build.MODEL,Tools.getLocalHostIp(),0,System.currentTimeMillis());
		userList.add(Tools.me.getCopy());
		adapterList = new ArrayList<Map<String, Object>>();
		Map map = new HashMap<String, Object>();
		map.put("headicon", Tools.headIconIds[Tools.me.getHeadIconPos()]);
		map.put("name", Tools.me.getName());
		map.put("ip", " IP:"+Tools.me.getIp());
		map.put("UnReadMsgCount", "");
		myicon.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		adapterList.add(map);
		//初始化view适配器
		adapter = new SimpleAdapter(this, adapterList, R.layout.item,
				new String[] {"headicon","name", "ip", "UnReadMsgCount" }, new int[] {
						R.id.headicon,R.id.name, R.id.ip, R.id.UnReadMsgCount });
		listView.setAdapter(adapter);
		titlename=(TextView)findViewById(R.id.friend_list_myName);
		titlename.setText(Tools.me.getName());
		proDia = new ProgressDialog(this);
		proDia.setTitle("文件发送");// 设置标题
		proDia.setMessage("文件");// 设置显示信息
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 水平进度条
		proDia.setMax(100);// 设置最大进度指
		proDia.setProgress(10);// 开始点
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.chart, menu);
		menu.add(0, 1, 1, "修改信息");
        //menu.add(0, 2, 2, "语音通话");
        return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            //修改信息
        	if(null==settingDialog)settingDialog = new SettingDialog(this);
    		settingDialog.show();
        }
        return true;
    }
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Tools.CMD_UPDATEINFORMATION:
				//更新自己信息
				reBroad();
				myicon.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
				break;
			case Tools.FLUSH:
				adapter.notifyDataSetChanged();
				break;
			case Tools.ADDUSER:
				adapterList.add((Map)msg.obj);
				adapter.notifyDataSetChanged();
				break;
			case Tools.SHOW:
				Toast.makeText(MainActivity.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case Tools.DESTROYUSER:
				int i=(Integer) msg.obj;
				userList.remove(i);
				adapterList.remove(i);
				adapter.notifyDataSetChanged();
			case Tools.REFLESHCOUNT:
				String ip=msg.obj.toString();
				Tools.out("刷新条目"+ip);
				for (int k = 0; k < adapterList.size(); k++) {
					if (adapterList.get(k).get("ip").equals("IP:"+ip)) 
					{ // 遍历
						if(Tools.msgContainer.get(ip)==null)
						{
							adapterList.get(k).put("UnReadMsgCount", "");	
						}
						else {
							adapterList.get(k).put("UnReadMsgCount", "未读:"+Tools.msgContainer.get(ip).size());	
							Tools.out("找到了:"+Tools.msgContainer.get(ip));
						}
					}
				}
				adapter.notifyDataSetChanged();
				break;
			case Tools.CMD_STARTTALK:
				//语音请求
				receiveCall((Msg)msg.obj);
				break;
			case Tools.CMD_STOPTALK:
				//语音结束
				if(revTalkDialog!=null)
					revTalkDialog.dismiss();
				break;
			case Tools.CMD_FINISHMEDIA:
				//来录音消息，并且已经接收完成，现在显示条目
				Msg m0=(Msg)msg.obj;
				String ip0=m0.getSendUserIp();
				Tools.out("刷新录音条目"+ip0);
				for (int k = 0; k < adapterList.size(); k++) {
					if (adapterList.get(k).get("ip").equals("IP:"+ip0)) 
					{ // 遍历
						if(Tools.msgContainer.get(ip0)==null)
						{
							adapterList.get(k).put("UnReadMsgCount", "");	
						}
						else {
							adapterList.get(k).put("UnReadMsgCount", "未读:"+Tools.msgContainer.get(ip0).size());	
							Tools.out("找到了:"+Tools.msgContainer.get(ip0));
						}
					}
				}
				adapter.notifyDataSetChanged();
				break;
			case Tools.CMD_FILEREQUEST:
				//文件请求
				receiveFile((Msg)msg.obj);
				break;
			case Tools.FILE_JINDU:
				String[] pi = ((String) msg.obj).split(Tools.sign);
				fileSize = Double.parseDouble(pi[2]);
				proDia.setTitle(pi[0]);// 设置标题
				proDia.setMessage(pi[1] + " 大小："
						+ FileAcitivity.getFormatSize(fileSize));// 设置显示信息
				proDia.onStart();
				proDia.show();
				break;
			case Tools.PROGRESS_FLUSH:
				int i0 = (int) ((Tools.sendProgress / (fileSize)) * 100);
				proDia.setProgress(i0);
				break;
			case Tools.PROGRESS_COL:// 关闭进度条
				proDia.dismiss();
				break;
			}
		}
	};
	// 收到传送文件请求
	private void receiveFile(Msg mes)
	{
		this.m=mes;
		String str=mes.getBody().toString();
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("是否接收文件：" + str.split(Tools.sign)[0] +" 大小："+FileAcitivity.getFormatSize(Double.parseDouble(str.split(Tools.sign)[1])))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("接受", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 接收文件 返回提示接受 建立tcp 服务器 接收文件
				FileTcpServer ts = new FileTcpServer(MainActivity.this);
				ts.start();
				Tools.sendProgress = 0;
				Message m1 = new Message();
				m1.what = Tools.FILE_JINDU;
				m1.obj = "接收文件" + Tools.sign + "正在接收：" + Tools.newfileName
						+ Tools.sign + Tools.newfileSize;
				handler.sendMessage(m1);
				fileProgress();// 启动进度条线程
				// 发送消息 让对方开始发送文件
				Msg msg=new Msg(0,Tools.me.getName(), Tools.me.getIp(), m.getSendUser(), m.getSendUserIp(),Tools.CMD_FILEACCEPT, null);
				tools.sendMsg(msg);
				return;
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 不接受 返回提示不接受
				Msg msg=new Msg(0,Tools.me.getName(), Tools.me.getIp(), m.getSendUser(), m.getSendUserIp(),Tools.CMD_FILEREFUSE, null);
				tools.sendMsg(msg);
				return;
			}
		}).show();
	}
	// 文件传送进度条
	public void fileProgress() {
		new Thread() {
			public void run() {
				while (Tools.sendProgress != -1) {
					Message m = new Message();
					m.what = Tools.PROGRESS_FLUSH;
					handler.sendMessage(m);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 关闭进度条
				Message m1 = new Message();
				m1.what = Tools.PROGRESS_COL;
				handler.sendMessage(m1);
			}
		}.start();
	}
	// 接收语音请求
	private void receiveCall(Msg mes)
	{
		if(!Tools.stoptalk){
			this.m=mes;
			AlertDialog.Builder  builder = new AlertDialog.Builder(this);
			builder.setTitle("来自:"+m.getSendUser());
			builder.setMessage(null);
			builder.setIcon(null);
			View vi = getLayoutInflater().inflate(R.layout.request_talk_layout, null);
			builder.setView(vi);
			revTalkDialog = builder.show();
			revTalkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					//发送结束呼叫请求
					Msg msg=new Msg();
					msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
					msg.setSendUserIp(Tools.me.getIp());
					msg.setReceiveUserIp(m.getSendUserIp());
					msg.setMsgType(Tools.CMD_STOPTALK);
					msg.setPackId(Tools.getTimel());
					tools.sendMsg(msg);
				}
			});
			Button talkOkBtn = (Button)vi.findViewById(R.id.receive_talk_okbtn);
			talkOkBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View okBtn) {
					if(!Tools.stoptalk){//如果远程用户未关闭通话，则向对方发送同意接收通话指令
						Msg msg=new Msg();
						msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
						msg.setSendUserIp(Tools.me.getIp());
						msg.setReceiveUserIp(m.getSendUserIp());
						msg.setMsgType(Tools.CMD_ACCEPTTALK);
						msg.setPackId(Tools.getTimel());
						tools.sendMsg(msg);
						okBtn.setEnabled(false);
						//同意接收并开始传输语音数据
						User person=new User(m.getSendUser(),m.getSendUserIp() );
						Tools.audio.audioSend(person);
					}
				}
			});
			Button talkCancelBtn = (Button)vi.findViewById(R.id.receive_talk_cancel);
			talkCancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View cancelBtn) {
					revTalkDialog.dismiss();
					
				}
			});
		}
	}
	@Override
    protected void onResume() {
    	super.onResume();
    	isPaused = false;
    	Tools.out("Resume");
    	reBroad();
		Tools.State=Tools.MAINACTIVITY;
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	isPaused = false;
    	Tools.out("PAUSE");
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	isPaused=true;
    	Tools.out("Destroy");
    }    
    //广播自己
    public void reBroad()
    {
    	//广播上线(包括自己)
		Msg msg=new Msg();
		msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
		msg.setHeadIconPos(Tools.me.getHeadIconPos());
		msg.setSendUserIp(Tools.me.getIp());
		msg.setReceiveUserIp(Tools.getBroadCastIP());
		msg.setMsgType(Tools.CMD_ONLINE);//通知上线命令
		msg.setPackId(Tools.getTimel());
		// 发送广播通知上线
		tools.sendMsg(msg);
    }
}
