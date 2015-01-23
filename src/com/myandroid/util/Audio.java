package com.myandroid.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Audio extends Thread {
	private ServerSocket sSocket = null;

	public Audio() {
	}

	@Override
	public void run() {
		super.run();
		try {
			sSocket = new ServerSocket(Tools.AUDIO_PORT);// 监听音频端口
			Tools.out("语音监听开启。");
			while (!sSocket.isClosed() && null != sSocket&&(!Tools.stoptalk)) {
				Socket socket = sSocket.accept();
				socket.setSoTimeout(5000);
				audioPlay(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 用来启动音频播放子线程
	public void audioPlay(Socket socket) {
		new AudioPlay(socket).start();
	}

	// 用来启动音频发送子线程
	public void audioSend(User person) {
		new AudioSend(person).start();
	}

	// 音频播线程
	public class AudioPlay extends Thread {
		Socket socket = null;

		public AudioPlay(Socket socket) {
			this.socket = socket;
			// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
		public void run() {
			super.run();
			try {
				InputStream is = socket.getInputStream();
				// 获得音频缓冲区大小
				int bufferSize = android.media.AudioTrack.getMinBufferSize(
						8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// 获得音轨对象
				AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC,
						8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize,
						AudioTrack.MODE_STREAM);

				// 设置喇叭音量
				player.setStereoVolume(1.0f, 1.0f);
				byte [] bytes_pkg = null ;
				// 开始播放声音
				player.play();
				byte[] audio = new byte[160];// 音频读取缓存
				int length = 0;

				while (!Tools.stoptalk) {
					length = is.read(audio);// 从网络读取音频数据
					//if (length > 0 && length % 2 == 0) {
						// for(int
						// i=0;i<length;i++)audio[i]=(byte)(audio[i]*2);//音频放大1倍
					//	player.write(audio, 0, length);// 播放音频数据
					//}
					bytes_pkg = audio.clone() ;
					player.write(bytes_pkg,0,bytes_pkg.length);
				}
				player.stop();
				is.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 音频发送线程
	public class AudioSend extends Thread {
		User person = null;

		public AudioSend(User person) {
			this.person = person;
			// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		}

		@Override
		public void run() {
			super.run();
			Socket socket = null;
			OutputStream os = null;
			AudioRecord recorder = null;
		    LinkedList<byte[]>  m_in_q ;
		    byte []     m_in_bytes ;
			try {
				socket = new Socket(person.getIp(), Tools.AUDIO_PORT);
				socket.setSoTimeout(5000);
				os = socket.getOutputStream();
				// 获得录音缓冲区大小
				int bufferSize = AudioRecord.getMinBufferSize(8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// 获得录音机对象
				recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize);
				m_in_bytes = new byte [bufferSize] ;
				m_in_q=new LinkedList<byte[]>();
				byte [] bytes_pkg ;
				recorder.startRecording();// 开始录音
				byte[] readBuffer = new byte[640];// 录音缓冲区
				int length = 0;
				while (!Tools.stoptalk) {
					//length = recorder.read(readBuffer, 0, 640);// 从mic读取音频数据
					
					//if (length > 0 && length % 2 == 0) {
					//	os.write(readBuffer, 0, length);// 写入到输出流，把音频数据通过网络发送给对方
					//}
					recorder.read(m_in_bytes, 0, bufferSize);
					bytes_pkg = m_in_bytes.clone() ;
					if(m_in_q.size() >= 2)
					{
					 os.write(m_in_q.removeFirst() , 0, m_in_q.removeFirst() .length);
					}  
					m_in_q.add(bytes_pkg) ;

				}
				recorder.stop();
				os.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void release() {
		try {
			System.out.println("Audio handler socket closed ...");
			if (null != sSocket)
				sSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
