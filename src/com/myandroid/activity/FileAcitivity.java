package com.myandroid.activity;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myandroid.message.R;
import com.myandroid.util.Tools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class FileAcitivity extends Activity {
	private Button shouYe = null;
	private Button shangYe = null;
	private Button duoXuan = null;
	private TextView state = null;
	private ListView listView = null;
	SimpleAdapter simpleAdapter = null;
	View popuView = null;
	PopupWindow popWin = null;
	public static final String ADDRESS = "/mnt";
	private int[] pic = new int[] { R.drawable.img_dir, R.drawable.img_file };
	private String choosePath;// ��¼ѡ�е��ļ�
	private String startPath = Tools.startPath;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_choose);
		init();
		showDir(startPath);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(choosePath!=null&&(new File(choosePath)).isFile()){
			menu.setHeaderTitle("�����˵�");
			menu.add(Menu.NONE, Menu.FIRST + 1, 1, "����");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			// �������������߽��� ȡ��ѡ�е��ļ�·��
			Intent it = FileAcitivity.this.getIntent();
			it.putExtra("path", choosePath);
			FileAcitivity.this.setResult(RESULT_OK, it);
			Tools.startPath=FileAcitivity.getStartPath(this.choosePath);
			FileAcitivity.this.finish();
		}
		return false;
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onContextMenuClosed(menu);
	}

	// ��ʼ
	public void init() {
		shouYe = (Button) super.findViewById(R.id.shouYe);
		shangYe = (Button) super.findViewById(R.id.shangYe);
		duoXuan = (Button) super.findViewById(R.id.duoXuan);
		state = (TextView) super.findViewById(R.id.state);
		listView = (ListView) super.findViewById(R.id.listView);
		duoXuan.setVisibility(8);
		// ΪlistViewע��˵�
		super.registerForContextMenu(this.listView);
		// ��Ұ��ť����
		shouYe.setOnClickListener(new OnClickListenerIter());
		// �ϲ㰴ť����
		shangYe.setOnClickListener(new OnClickListenerIterShangYe());
		// �����б���
		listView.setOnItemClickListener(new OnItemClickListenerIter());
		// �б��������
		listView.setOnItemLongClickListener(new OnItemLongClickListenerml());
	}

	// �б��������
	class OnItemLongClickListenerml implements OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			Map<String, String> map = (Map<String, String>) arg0.getAdapter()
					.getItem(arg2);
			String name = state.getText().toString() + "/" + map.get("name");
			if (name.equals("/mnt/secure") || name.equals("/mnt/usb_storage")
					|| name.equals("/mnt/sdcard/.android_secure")) {
				Toast.makeText(FileAcitivity.this, "�޷���", Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			choosePath = name;
			// Toast.makeText(FileChoose.this, "׼�������ļ���" + choosePath,
			// Toast.LENGTH_SHORT).show();
			return false;
		}

	}

	// �ϲ㰴ť����
	class OnClickListenerIterShangYe implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			String p = state.getText().toString();
			if (p.equals("/mnt")) {
				return;
			} else {
				p = new File(p).getParent();
				choosePath=p;
				showDir(p);
			}
		}

	}

	// �б������
	class OnItemClickListenerIter implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Map<String, String> map = (Map<String, String>) arg0.getAdapter()
					.getItem(arg2);
			String name = state.getText().toString() + "/" + map.get("name");
			if (name.equals("/mnt/secure") || name.equals("/mnt/usb_storage")
					|| name.equals("/mnt/sdcard/.android_secure")) {
				Toast.makeText(FileAcitivity.this, "�޷���", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			File f = new File(name);
			if (f.isFile()) {// ���ѡ�е����ļ�
				FileAcitivity.openFile(FileAcitivity.this,f);
				return;
			} else {// ���ѡ�е����ļ���
					// state.setText(f.getPath());
				choosePath=f.getPath();
				showDir(f.getPath());
			}
		}

	}

	class OnClickListenerIter implements android.view.View.OnClickListener {
		public void onClick(View v) {
			choosePath=ADDRESS;
			showDir(ADDRESS);
		}
	}

	// ��ʾĿ¼
	public void showDir(String path) {
		state.setText(path);
		File f = new File(path);
		File[] fileList = f.listFiles();
		fileList = this.fileStyle(fileList);
		List list = new ArrayList<Map<String, String>>();
		for (int i = 0; i < fileList.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			if (fileList[i].isDirectory()) {
				map.put("img", String.valueOf(pic[0]));
				map.put("size", "�ļ���");

			} else {
				map.put("img", String.valueOf(pic[1]));
				map.put("size", "��С��" + getFormatSize(fileList[i].length()));
			}
			map.put("name", fileList[i].getName());
			map.put("time", "ʱ�䣺" + timeFormat(fileList[i].lastModified()));
			list.add(map);

		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(FileAcitivity.this, list,
				R.layout.file_show, new String[] { "img", "name", "size",
						"time" }, new int[] { R.id.img, R.id.name, R.id.size,
						R.id.time });
		FileAcitivity.this.listView.setAdapter(simpleAdapter);
	}

	// �ļ�����
	public File[] fileStyle(File[] files) {
		List<File> fileList = new ArrayList<File>();
		List<File> dirList = new ArrayList<File>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {// ������ļ�����remove
				dirList.add(files[i]);// �����ļ���list
			} else {
				fileList.add(files[i]);// ɾ�����ļ�list
			}
		}
		dirList = this.fileOrder(dirList);
		fileList = this.fileOrder(fileList);
		dirList.addAll(fileList);
		for (int i = 0; i < dirList.size(); i++) {
			files[i] = dirList.get(i);
		}
		// ((ArrayList<File>) dirList).trimToSize();
		return files;
	}

	// �ļ�����
	public List<File> fileOrder(List<File> list) {
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = 0; j < list.size() - i - 1; j++) {
				if (list.get(j).getName().charAt(0) > list.get(j + 1).getName()
						.charAt(0)) {
					list.add(j + 2, list.get(j));
					list.remove(j);
				}
			}
		}
		return list;
	}

	// ��ʽ��ʱ��
	public String timeFormat(long date) {
		Date d = new Date(date);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		return sdf.format(d);
	}

	// �����ļ���С
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			return size + "Byte(s)";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "KB";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "MB";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
				+ "TB";
	}
	public void onBackPressed() {
		Tools.startPath=FileAcitivity.getStartPath(choosePath);
		FileAcitivity.this.finish();
	}
	// ��ó�ʼĿ¼-ʵ�ֵڶ��δ��Ǿͽ���Ŀ¼
	public static String getStartPath(String startPath) {
		if (startPath == null) {
			return FileAcitivity.ADDRESS;
		} else {
			File file = new File(startPath);
			if (file.isFile()) {
				return file.getParent();
			} else {
				return startPath;
			}
		}
	}
	public static void openFile(Activity a,File file){ 
	     
	    Intent intent = new Intent(); 
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    //����intent��Action���� 
	    intent.setAction(Intent.ACTION_VIEW); 
	    //��ȡ�ļ�file��MIME���� 
	    String type = getMIMEType(file); 
	    //����intent��data��Type���ԡ� 
	    intent.setDataAndType(/*uri*/Uri.fromFile(file), type); 
	    //��ת 
	    a.startActivity(intent);   
	     
	} 
	private static String getMIMEType(File file) { 
	     
	    String type="*/*"; 
	    String fName = file.getName(); 
	    //��ȡ��׺��ǰ�ķָ���"."��fName�е�λ�á� 
	    int dotIndex = fName.lastIndexOf("."); 
	    if(dotIndex < 0){ 
	        return type; 
	    } 
	    /* ��ȡ�ļ��ĺ�׺��*/ 
	    String end=fName.substring(dotIndex,fName.length()).toLowerCase(); 
	    if(end=="")return type; 
	    //��MIME���ļ����͵�ƥ������ҵ���Ӧ��MIME���͡� 
	    for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??��������һ�������ʣ����MIME_MapTable��ʲô�� 
	        if(end.equals(MIME_MapTable[i][0])) 
	            type = MIME_MapTable[i][1]; 
	    }        
	    return type; 
	} 
	private static final String[][] MIME_MapTable={ 
        //{��׺����MIME����} 
        {".3gp",    "video/3gpp"}, 
        {".apk",    "application/vnd.android.package-archive"}, 
        {".asf",    "video/x-ms-asf"}, 
        {".avi",    "video/x-msvideo"}, 
        {".bin",    "application/octet-stream"}, 
        {".bmp",    "image/bmp"}, 
        {".c",  "text/plain"}, 
        {".class",  "application/octet-stream"}, 
        {".conf",   "text/plain"}, 
        {".cpp",    "text/plain"}, 
        {".doc",    "application/msword"}, 
        {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}, 
        {".xls",    "application/vnd.ms-excel"},  
        {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}, 
        {".exe",    "application/octet-stream"}, 
        {".gif",    "image/gif"}, 
        {".gtar",   "application/x-gtar"}, 
        {".gz", "application/x-gzip"}, 
        {".h",  "text/plain"}, 
        {".htm",    "text/html"}, 
        {".html",   "text/html"}, 
        {".jar",    "application/java-archive"}, 
        {".java",   "text/plain"}, 
        {".jpeg",   "image/jpeg"}, 
        {".jpg",    "image/jpeg"}, 
        {".js", "application/x-javascript"}, 
        {".log",    "text/plain"}, 
        {".m3u",    "audio/x-mpegurl"}, 
        {".m4a",    "audio/mp4a-latm"}, 
        {".m4b",    "audio/mp4a-latm"}, 
        {".m4p",    "audio/mp4a-latm"}, 
        {".m4u",    "video/vnd.mpegurl"}, 
        {".m4v",    "video/x-m4v"},  
        {".mov",    "video/quicktime"}, 
        {".mp2",    "audio/x-mpeg"}, 
        {".mp3",    "audio/x-mpeg"}, 
        {".mp4",    "video/mp4"}, 
        {".mpc",    "application/vnd.mpohun.certificate"},        
        {".mpe",    "video/mpeg"},   
        {".mpeg",   "video/mpeg"},   
        {".mpg",    "video/mpeg"},   
        {".mpg4",   "video/mp4"},    
        {".mpga",   "audio/mpeg"}, 
        {".msg",    "application/vnd.ms-outlook"}, 
        {".ogg",    "audio/ogg"}, 
        {".pdf",    "application/pdf"}, 
        {".png",    "image/png"}, 
        {".pps",    "application/vnd.ms-powerpoint"}, 
        {".ppt",    "application/vnd.ms-powerpoint"}, 
        {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, 
        {".prop",   "text/plain"}, 
        {".rc", "text/plain"}, 
        {".rmvb",   "audio/x-pn-realaudio"}, 
        {".rtf",    "application/rtf"}, 
        {".sh", "text/plain"}, 
        {".tar",    "application/x-tar"},    
        {".tgz",    "application/x-compressed"},  
        {".txt",    "text/plain"}, 
        {".wav",    "audio/x-wav"}, 
        {".wma",    "audio/x-ms-wma"}, 
        {".wmv",    "audio/x-ms-wmv"}, 
        {".wps",    "application/vnd.ms-works"}, 
        {".xml",    "text/plain"}, 
        {".z",  "application/x-compress"}, 
        {".zip",    "application/x-zip-compressed"}, 
        {"",        "*/*"}   
    }; 
}
