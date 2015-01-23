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
			sSocket = new ServerSocket(Tools.AUDIO_PORT);// ������Ƶ�˿�
			Tools.out("��������������");
			while (!sSocket.isClosed() && null != sSocket&&(!Tools.stoptalk)) {
				Socket socket = sSocket.accept();
				socket.setSoTimeout(5000);
				audioPlay(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ����������Ƶ�������߳�
	public void audioPlay(Socket socket) {
		new AudioPlay(socket).start();
	}

	// ����������Ƶ�������߳�
	public void audioSend(User person) {
		new AudioSend(person).start();
	}

	// ��Ƶ���߳�
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
				// �����Ƶ��������С
				int bufferSize = android.media.AudioTrack.getMinBufferSize(
						8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// ����������
				AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC,
						8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize,
						AudioTrack.MODE_STREAM);

				// ������������
				player.setStereoVolume(1.0f, 1.0f);
				byte [] bytes_pkg = null ;
				// ��ʼ��������
				player.play();
				byte[] audio = new byte[160];// ��Ƶ��ȡ����
				int length = 0;

				while (!Tools.stoptalk) {
					length = is.read(audio);// �������ȡ��Ƶ����
					//if (length > 0 && length % 2 == 0) {
						// for(int
						// i=0;i<length;i++)audio[i]=(byte)(audio[i]*2);//��Ƶ�Ŵ�1��
					//	player.write(audio, 0, length);// ������Ƶ����
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

	// ��Ƶ�����߳�
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
				// ���¼����������С
				int bufferSize = AudioRecord.getMinBufferSize(8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

				// ���¼��������
				recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, bufferSize);
				m_in_bytes = new byte [bufferSize] ;
				m_in_q=new LinkedList<byte[]>();
				byte [] bytes_pkg ;
				recorder.startRecording();// ��ʼ¼��
				byte[] readBuffer = new byte[640];// ¼��������
				int length = 0;
				while (!Tools.stoptalk) {
					//length = recorder.read(readBuffer, 0, 640);// ��mic��ȡ��Ƶ����
					
					//if (length > 0 && length % 2 == 0) {
					//	os.write(readBuffer, 0, length);// д�뵽�����������Ƶ����ͨ�����緢�͸��Է�
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
