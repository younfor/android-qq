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
	//����
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
	public String choosePath = null;// ѡ�е��ļ�
	ProgressDialog proDia = null;
	Double fileSize=0.0;
	// ������Ļ
	private final Handler mHandler = new Handler();
    private Runnable scrollRunnable= new Runnable() {
	    @Override
	    public void run() {
            int offset = chartMsgPanel.getMeasuredHeight() - chartMsgScroll.getHeight();//�жϸ߶� 
            if (offset > 0) {
	            chartMsgScroll.scrollBy(0, 100);//ÿ�ι�100����λ
	        }
	    }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		//��ʼ��
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
				mediadialog = new MediaDialog(ChatActivity.this, "����¼��");
				chartMediaRecord.setText("����¼��...");
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
					chartMediaRecord.setText("¼��");
					mediadialog.dismiss();
					//��ʾ¼���������͸��Է�
					sendMediaRecord(media.name);
					break;
				}
				return false;
			}
		});
		tools=new Tools(this,Tools.ACTIVITY_CHART);
		//��ȡ���������Ϣ
		List<Msg> list=Tools.msgContainer.get(person.getIp());
		if(list!=null)
		{
			for(int i=0;i<list.size();i++)
			{
				if(list.get(i).getMsgType()==Tools.ISFILE)
				{
					//����������ļ�
					receiveMedia(list.get(i));
				}
				else
				{
					//�������Ϣ
					receiveMsg(list.get(i));
				}
			}
			Tools.msgContainer.remove(person.getIp());
			Tools.currentUserIp=person.getIp();
		}
		proDia = new ProgressDialog(this);
		proDia.setTitle("�ļ�����");// ���ñ���
		proDia.setMessage("�ļ�");// ������ʾ��Ϣ
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// ˮƽ������
		proDia.setMax(100);// ����������ָ
		proDia.setProgress(10);// ��ʼ��
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.chart, menu);
		menu.add(0, 1, 1, "�����ļ�");
        menu.add(0, 2, 2, "����ͨ��");
        return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            //�����ļ�
			Intent it = new Intent(ChatActivity.this, FileAcitivity.class);
			ChatActivity.this.startActivityForResult(it, 1);
        }
        else if(item.getItemId() == 2){
            //����ͨ��
        	Tools.out("��ʼ����:"+person.getIp());
			sendTalk();
        } 
        return true;
    }
	// ��ť����¼�
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.chart_msg_send:
				//������Ϣ��ť
				sendMsg();
				break; 
		}
	}
	// ����¼��
	public void sendMediaRecord(String name)
	{
		Tools.out("¼���ļ���:"+name);
		Tools.currentPath=media.sendpath+"/"+name;
		//��view����ʾ��������Բ���
		View view = getLayoutInflater().inflate(R.layout.send_msg_layout, null);
		TextView smcView = (TextView)view.findViewById(R.id.send_msg_content);
		TextView smtView = (TextView)view.findViewById(R.id.send_msg_time);
		TextView nView = (TextView)view.findViewById(R.id.send_nickename);
		ImageView senduserhead=(ImageView)view.findViewById(R.id.senduserhead);
		senduserhead.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		MediaPlayer mp = MediaPlayer.create(ChatActivity.this, Uri.parse(media.sendpath+"/"+name));
		int duration = mp.getDuration()/1000;//��Ϊʱ�� ��ms 
		//�õ�¼��ʱ��
		smcView.setText(duration+"��((");
		smcView.setTag(name);
		smcView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mediaName=v.getTag().toString();
				//����¼���ļ���������
				media.startPlay(media.sendpath+"/"+mediaName);
			}
		});
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(Tools.me.getName());
		chartMsgPanel.addView(view);
		//֪ͨ׼������¼��
		String add=media.sendpath+"/"+name;
		String body=(new File(add)).getName()+Tools.sign+(new File(add)).length();
		Tools.out("body:"+body);
		Msg msg=new Msg(Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_SENDMEDIA, body);
		tools.sendMsg(msg);
		//���¹�����
		mHandler.post(scrollRunnable);
	}
	// ��������
	public void sendTalk()
	{
		AlertDialog.Builder  builder = new AlertDialog.Builder(this);
		builder.setTitle(Tools.me.getName());
		builder.setMessage(person.getName());
		builder.setIcon(null);
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface cdialog, int which) {
				cdialog.dismiss();
				Tools.out("ȡ������");
			}
		});
		callDialog = builder.show();
		callDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				//���ͽ�����������
				Msg msg=new Msg();
				msg.setSendUser(Tools.me.getName());//�ǳ�Ĭ��Ϊ�Լ��Ļ�����
				msg.setSendUserIp(Tools.me.getIp());
				msg.setReceiveUserIp(person.getIp());
				msg.setMsgType(Tools.CMD_STOPTALK);//���ͺ�������
				msg.setPackId(Tools.getTimel());
				tools.sendMsg(msg);
			}
		});
		//���ͺ�������
		Msg msg=new Msg();
		msg.setSendUser(Tools.me.getName());//�ǳ�Ĭ��Ϊ�Լ��Ļ�����
		msg.setSendUserIp(Tools.me.getIp());
		msg.setReceiveUserIp(person.getIp());
		msg.setMsgType(Tools.CMD_STARTTALK);//���ͺ�������
		msg.setPackId(Tools.getTimel());
		tools.sendMsg(msg);
		Tools.out("��ʼ����");
	}
	// ������Ϣ
	public void sendMsg()
	{
		String body = chartMsg.getText().toString();
		if(null==body || body.length()<=0){
			Toast.makeText(this,"����Ϊ��", Toast.LENGTH_SHORT).show();
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
		//���¹�����
		mHandler.post(scrollRunnable);
	}
	// ������Ϣ
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
	// ����¼��
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
		int duration = mp.getDuration()/1000;//��Ϊʱ�� ��ms 
		//�õ�¼��ʱ��
		smcView.setText(duration+"��((");
		smcView.setTag(fileInfo[0]);
		smcView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mediaName=v.getTag().toString();
				//����¼���ļ���������
				media.startPlay(media.receivepath+"/"+mediaName);
			}
		});
		smtView.setText(Tools.getChangeTime(Tools.getTimel()));
		nView.setText(m.getSendUser());
		chartMsgPanel.addView(view);
		mHandler.post(scrollRunnable);
	}
	// ������������
	private void receiveCall(Msg msg)
	{
		if(!Tools.stoptalk){
			AlertDialog.Builder  builder = new AlertDialog.Builder(this);
			builder.setTitle("����:"+msg.getSendUser());
			builder.setMessage(null);
			builder.setIcon(null);
			View vi = getLayoutInflater().inflate(R.layout.request_talk_layout, null);
			builder.setView(vi);
			revTalkDialog = builder.show();
			revTalkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					//���ͽ�����������
					Msg msg=new Msg();
					msg.setSendUser(Tools.me.getName());//�ǳ�Ĭ��Ϊ�Լ��Ļ�����
					msg.setSendUserIp(Tools.me.getIp());
					msg.setReceiveUserIp(person.getIp());
					msg.setMsgType(Tools.CMD_STOPTALK);//���ͺ�������
					msg.setPackId(Tools.getTimel());
					tools.sendMsg(msg);
				}
			});
			Button talkOkBtn = (Button)vi.findViewById(R.id.receive_talk_okbtn);
			talkOkBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View okBtn) {
					if(!Tools.stoptalk){//���Զ���û�δ�ر�ͨ��������Է�����ͬ�����ͨ��ָ��
						Msg msg=new Msg();
						msg.setSendUser(Tools.me.getName());//�ǳ�Ĭ��Ϊ�Լ��Ļ�����
						msg.setSendUserIp(Tools.me.getIp());
						msg.setReceiveUserIp(person.getIp());
						msg.setMsgType(Tools.CMD_ACCEPTTALK);
						msg.setPackId(Tools.getTimel());
						tools.sendMsg(msg);
						okBtn.setEnabled(false);
						//ͬ����ղ���ʼ������������
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
				//������Ϣ
				receiveMsg((Msg)msg.obj);
				break;
			case Tools.CMD_STARTTALK:
				//��������
				receiveCall((Msg)msg.obj);
				break;
			case Tools.CMD_STOPTALK:
				//��������
				if(revTalkDialog!=null)
					revTalkDialog.dismiss();
				if(callDialog!=null)
					callDialog.dismiss();
				break;
			case Tools.CMD_FINISHMEDIA:
				//��ɽ���¼��
				receiveMedia((Msg)msg.obj);
				break;
			case Tools.FILE_JINDU:
				String[] pi = ((String) msg.obj).split(Tools.sign);
				fileSize = Double.parseDouble(pi[2]);
				proDia.setTitle(pi[0]);// ���ñ���
				proDia.setMessage(pi[1] + " ��С��"
						+ FileAcitivity.getFormatSize(fileSize));// ������ʾ��Ϣ
				proDia.onStart();
				proDia.show();
				break;
			case Tools.PROGRESS_FLUSH:
				int i0 = (int) ((Tools.sendProgress / (fileSize)) * 100);
				proDia.setProgress(i0);
				break;
			case Tools.PROGRESS_COL:// �رս�����
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
    // �����ļ�ȡ��·��
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		switch (resultCode) {
 		case RESULT_OK:
 			choosePath = data.getStringExtra("path");
 			Tools.out("�ļ�·��:"+choosePath);
 			//�����������ļ�
 			Msg msg=new Msg(Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), person.getName(), person.getIp(),Tools.CMD_FILEREQUEST, 
 					(new File(choosePath)).getName()+Tools.sign+(new File(choosePath)).length());
 			tools.sendMsg(msg);
 			Tools.out("�����������ļ�");
 		}
 	}
}
