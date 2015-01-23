package com.myandroid.activity;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.myandroid.message.R;
import com.myandroid.util.Media;
import com.myandroid.util.Msg;
import com.myandroid.util.Tools;
import com.myandroid.util.User;

import android.media.MediaPlayer;
import android.net.Uri;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements OnClickListener{
	//参数
	private LinearLayout chartMsgPanel = null;
	private ScrollView chartMsgScroll = null;
	private User person=null;
	private EditText chartMsg = null;
	private Button chartMsgSend = null;
	//private Button chartMsgFile = null;
	//private Button chartTalk=null;
	private Button chartMediaRecord=null;
	public Tools tools=null;
	AlertDialog revTalkDialog=null,callDialog=null;
	MediaDialog mediadialog=null;
	Media media=new Media();
	public String choosePath = null;// 选中的文件
	ProgressDialog proDia = null;
	Double fileSize=0.0;
	// 滚动屏幕
	private final Handler mHandler = new Handler();
    private Runnable scrollRunnable= new Runnable() {
	    @Override
	    public void run() {
            int offset = chartMsgPanel.getMeasuredHeight() - chartMsgScroll.getHeight();//判断高度 
            if (offset > 0) {
	            chartMsgScroll.scrollBy(0, 100);//每次滚100个单位
	        }
	    }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		//初始化
		Tools.State=Tools.CHATACTIVITY;
		Tools.chart=this;
		setContentView(R.layout.activity_chart);
		Intent intent = getIntent();
		person = (User)intent.getExtras().getSerializable("person");
		((TextView)findViewById(R.id.my_nickename)).setText(person.getName());
		//chartTalk=(Button)findViewById(R.id.chart_talk);
		//chartTalk.setOnClickListener(this);
		chartMsg = (EditText)findViewById(R.id.chart_msg);
		chartMsgSend = (Button)findViewById(R.id.chart_msg_send);
		chartMsgSend.setOnClickListener(this);
		//chartMsgFile = (Button)findViewById(R.id.chart_msg_file);
		//chartMsgFile.setOnClickListener(this);
		chartMsgPanel = (LinearLayout)findViewById(R.id.chart_msg_panel);
		chartMsgScroll = (ScrollView)findViewById(R.id.chart_msg_scroll);
		chartMediaRecord=(Button)findViewById(R.id.chart_media_record);
		chartMediaRecord.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mediadialog = new MediaDialog(ChatActivity.this, "正在录音");
				chartMediaRecord.setText("正在录音...");
				media.startRecord();
				mediadialog.show();
				return false;
			}
		});
		chartMediaRecord.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					media.stopRecord();
					chartMediaRecord.setText("录音");
					mediadialog.dismiss();
					//显示录音，并发送给对方
					sendMediaRecord(media.name);
					break;
				}
				return false;
			}
		});
		tools=new Tools(this,Tools.ACTIVITY_CHART);
		//读取并清空新消息
		List<Msg> list=Tools.msgContainer.get(person.getIp());
		if(list!=null)
		{
			for(int i=0;i<list.size();i++)
			{
				if(list.get(i).getMsgType()==Tools.ISFILE)
				{
					//如果是语音文件
					receiveMedia(list.get(i));
				}
				else
				{
					//如果是信息
					receiveMsg(list.get(i));
				}
			}
			Tools.msgContainer.remove(person.getIp());
			Tools.currentUserIp=person.getIp();
		}
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
		menu.add(0, 1, 1, "发送文件");
        menu.add(0, 2, 2, "语音通话");
        return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            //发送文件
			Intent it = new Intent(ChatActivity.this, FileAcitivity.class);
			ChatActivity.this.startActivityForResult(it, 1);
        }
        else if(item.getItemId() == 2){
            //语音通话
        	Tools.out("开始语音:"+person.getIp());
			sendTalk();
        } 
        return true;
    }
	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.chart_msg_send:
				//发送信息按钮
				sendMsg();
				break; 
		}
	}
	// 发送录音
	public void sendMediaRecord(String name)
	{
		Tools.out("录音文件名:"+name);
		Tools.currentPath=media.sendpath+"/"+name;
		//在view中显示，点击可以播放
		View view = getLayoutInflater().inflate(R.layout.send_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.send_msg_content);
		TextView smtView = (TextView)view.findViewById(R.id.send_msg_time);
		TextView nView = (TextView)view.findViewById(R.id.send_nickename);
		ImageView senduserhead=(ImageView)view.findViewById(R.id.senduserhead);
		senduserhead.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		MediaPlayer mp = MediaPlayer.create(ChatActivity.this, Uri.parse(media.sendpath+"/"+name));
		int duration = mp.getDuration()/1000;//即为时长 是ms 
		//得到录音时间
		smcView.setText(duration+"″((");
		smcView.setTag(name);
		smcView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mediaName=v.getTag().toString();
				//传入录音文件名并播放
				media.startPlay(media.sendpath+"/"+mediaName);
			}
		});
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(Tools.me.getName());
		chartMsgPanel.addView(view);
		//通知准备发送录音
		String add=media.sendpath+"/"+name;
		String body=(new File(add)).getName()+Tools.sign+(new File(add)).length();
		Tools.out("body:"+body);
		Msg msg=new Msg(Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_SENDMEDIA, body);
		tools.sendMsg(msg);
		//更新滚动条
		mHandler.post(scrollRunnable);
	}
	// 发送语音
	public void sendTalk()
	{
		AlertDialog.Builder  builder = new AlertDialog.Builder(this);
		builder.setTitle(Tools.me.getName());
		builder.setMessage(person.getName());
		builder.setIcon(null);
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface cdialog, int which) {
				cdialog.dismiss();
				Tools.out("取消语音");
			}
		});
		callDialog = builder.show();
		callDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				//发送结束呼叫请求
				Msg msg=new Msg();
				msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
				msg.setSendUserIp(Tools.me.getIp());
				msg.setReceiveUserIp(person.getIp());
				msg.setMsgType(Tools.CMD_STOPTALK);//发送呼叫请求
				msg.setPackId(Tools.getTimel());
				tools.sendMsg(msg);
			}
		});
		//发送呼叫请求
		Msg msg=new Msg();
		msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
		msg.setSendUserIp(Tools.me.getIp());
		msg.setReceiveUserIp(person.getIp());
		msg.setMsgType(Tools.CMD_STARTTALK);//发送呼叫请求
		msg.setPackId(Tools.getTimel());
		tools.sendMsg(msg);
		Tools.out("开始呼叫");
	}
	// 发送信息
	public void sendMsg()
	{
		String body = chartMsg.getText().toString();
		if(null==body || body.length()<=0){
			Toast.makeText(this,"不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		chartMsg.setText("");
		View view = getLayoutInflater().inflate(R.layout.send_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.send_msg_content);
		TextView smtView = (TextView)view.findViewById(R.id.send_msg_time);
		TextView nView = (TextView)view.findViewById(R.id.send_nickename);
		ImageView senduserhead=(ImageView)view.findViewById(R.id.senduserhead);
		senduserhead.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		Msg msg=new Msg(Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_SENDMSG, body);
		smcView.setText(body);
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(Tools.me.getName());
		chartMsgPanel.addView(view);
		tools.sendMsg(msg);
		//更新滚动条
		mHandler.post(scrollRunnable);
	}
	// 接收信息
	private void receiveMsg(Msg m)
	{
		View view = getLayoutInflater().inflate(R.layout.received_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.received_msg_content);
		TextView smtView = (TextView)view.findViewById(R.id.received_msg_time);
		TextView nView = (TextView)view.findViewById(R.id.received_nickename);
		ImageView receiveIcon=(ImageView)view.findViewById(R.id.receiveuserhead);
		receiveIcon.setImageResource(Tools.headIconIds[m.getHeadIconPos()]);
		smcView.setText((String)m.getBody());
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(m.getSendUser());
		chartMsgPanel.addView(view);
		mHandler.post(scrollRunnable);
	}
	// 接收录音
	private void receiveMedia(Msg m)
	{
		View view = getLayoutInflater().inflate(R.layout.received_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.received_msg_content);
		TextView smtView = (TextView)view.findViewById(R.id.received_msg_time);
		TextView nView = (TextView)view.findViewById(R.id.received_nickename);
		ImageView receiveIcon=(ImageView)view.findViewById(R.id.receiveuserhead);
		receiveIcon.setImageResource(Tools.headIconIds[m.getHeadIconPos()]);
		String[] fileInfo = (m.getBody().toString()).split(Tools.sign);
		MediaPlayer mp = MediaPlayer.create(ChatActivity.this, Uri.parse(media.receivepath+"/"+fileInfo[0]));
		int duration = mp.getDuration()/1000;//即为时长 是ms 
		//得到录音时间
		smcView.setText(duration+"″((");
		smcView.setTag(fileInfo[0]);
		smcView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mediaName=v.getTag().toString();
				//传入录音文件名并播放
				media.startPlay(media.receivepath+"/"+mediaName);
			}
		});
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(m.getSendUser());
		chartMsgPanel.addView(view);
		mHandler.post(scrollRunnable);
	}
	// 接收语音请求
	private void receiveCall(Msg msg)
	{
		if(!Tools.stoptalk){
			AlertDialog.Builder  builder = new AlertDialog.Builder(this);
			builder.setTitle("来自:"+msg.getSendUser());
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
					msg.setReceiveUserIp(person.getIp());
					msg.setMsgType(Tools.CMD_STOPTALK);//发送呼叫请求
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
						msg.setReceiveUserIp(person.getIp());
						msg.setMsgType(Tools.CMD_ACCEPTTALK);
						msg.setPackId(Tools.getTimel());
						tools.sendMsg(msg);
						okBtn.setEnabled(false);
						//同意接收并开始传输语音数据
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
	// Handler
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Tools.SHOW:
				Toast.makeText(ChatActivity.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case Tools.RECEIVEMSG:
				//接收消息
				receiveMsg((Msg)msg.obj);
				break;
			case Tools.CMD_STARTTALK:
				//语音请求
				receiveCall((Msg)msg.obj);
				break;
			case Tools.CMD_STOPTALK:
				//语音结束
				if(revTalkDialog!=null)
					revTalkDialog.dismiss();
				if(callDialog!=null)
					callDialog.dismiss();
				break;
			case Tools.CMD_FINISHMEDIA:
				//完成接收录音
				receiveMedia((Msg)msg.obj);
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
	@Override
    protected void onResume() {
    	super.onResume();
		Tools.State=Tools.CHATACTIVITY;
    }
	@Override
    protected void onPause() {
    	super.onPause();
    	Tools.State=Tools.MAINACTIVITY;
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Tools.pretime=0;
    	Tools.out("destroy");
    	Tools.State=Tools.MAINACTIVITY;
    }    
    // 发送文件取得路径
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		switch (resultCode) {
 		case RESULT_OK:
 			choosePath = data.getStringExtra("path");
 			Tools.out("文件路径:"+choosePath);
 			//发送请求传送文件
 			Msg msg=new Msg(Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_FILEREQUEST, 
 					(new File(choosePath)).getName()+Tools.sign+(new File(choosePath)).length());
 			tools.sendMsg(msg);
 			Tools.out("发送请求传送文件");
 		}
 	}
}
