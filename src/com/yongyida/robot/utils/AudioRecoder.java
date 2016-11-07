/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 *
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 *
 */
package com.yongyida.robot.utils;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;

import net.surina.soundtouch.AudioConfig;
import net.surina.soundtouch.log;

/**
 * 音频录制
 *
 */
public class AudioRecoder {
	private static final String TAG = "AudioRecoder";

	private AudioRecord mAudioRecord;
	private int mAudioBufferSize = 0;
	private boolean mIsOpened = false;
	private Thread mThread;
	private RecordListener mRecordListener;

	private boolean mEnableAudioEffect = true;
	private AutomaticGainControl mAutomaticGainControl;    // 自动增益控制
	private NoiseSuppressor mNoiseSuppressor;              // 噪声抑制
	private AcousticEchoCanceler mAcousticEchoCanceler;    // 回声消除

	public AudioRecoder() {
	}

	public boolean isOpened() {
		return mIsOpened;
	}

	public int getAudioBufferSize() {
		return mAudioBufferSize;
	}

	/**
	 * 返回是否使用音效
	 * @param
	 * @return boolean
	 */
	public boolean isEnableAudioEffect() {
		return mEnableAudioEffect;
	}

	/**
	 * 设置是否使用音效
	 * @param enable
	 * @return
	 */
	public void setEnableAudioEffect(boolean enable) {
		mEnableAudioEffect = enable;
	}

	/**
	 * 设置录音监听
	 * @param listener
	 * @return
	 */
	public void setRecordListener(RecordListener listener) {
		mRecordListener = listener;
	}

	/**
	 * 打开录音
	 * @param
	 * @return boolean
	 */
	public boolean open() {
		log.d(TAG, "open()");

		if (!mIsOpened) {
			//计算音频缓冲区大小
			mAudioBufferSize = AudioRecord.getMinBufferSize(
					AudioConfig.SAMPLE_RATE,
					AudioConfig.RECORDER_CHANNEL_CONFIG,
					AudioConfig.AUDIO_FORMAT);
			log.d(TAG, "AudioBufferSize: " + mAudioBufferSize);
			if (mAudioBufferSize == AudioRecord.ERROR_BAD_VALUE) {
				log.e(TAG, "mAudioBufferSize error");
				return false;
			}

			//建立音频录制
			mAudioRecord = new AudioRecord(AudioConfig.AUDIO_SOURCE,
					AudioConfig.SAMPLE_RATE,
					AudioConfig.RECORDER_CHANNEL_CONFIG,
					AudioConfig.AUDIO_FORMAT,
					mAudioBufferSize);

			//设置音效
			if (mEnableAudioEffect) {
				setAudioEffect(mAudioRecord.getAudioSessionId(), mEnableAudioEffect);
			}

			mIsOpened = true;

			//启动录音线程
			mThread = new Thread(new recordRunnable());
			mThread.start();
		}
		return mIsOpened;
	}

	/**
	 * 关闭录音
	 * @param
	 * @return
	 */
	public void close() {
		log.d(TAG, "close()");

		if (mIsOpened) {
			mIsOpened = false;
			if (mThread != null && mThread.isAlive()) {
				mThread.interrupt();
				mThread = null;
			}
		}
	}

	/**
	 * 设置音效
	 * @param audioSessionId
	 * @param enable
	 * @return
	 */
	private void setAudioEffect(int audioSessionId, boolean enable) {
		// 自动增益控制
		try {
			log.d(TAG, "AutomaticGainControl isAvailable: " + AutomaticGainControl.isAvailable());
			if (AutomaticGainControl.isAvailable()) {
				mAutomaticGainControl = AutomaticGainControl.create(audioSessionId);
				mAutomaticGainControl.setEnabled(enable);
				log.d(TAG, "AutomaticGainControl enable: " + mAutomaticGainControl.getEnabled());
			}
		}
		catch (Exception e) {
			log.e(TAG, "AutomaticGainControl exception: " + e);
		}

		// 噪声抑制
		try {
			log.d(TAG, "NoiseSuppressor isAvailable: " + NoiseSuppressor.isAvailable());
			if (NoiseSuppressor.isAvailable()) {
				mNoiseSuppressor = NoiseSuppressor.create(audioSessionId);
				mNoiseSuppressor.setEnabled(enable);
				log.d(TAG, "NoiseSuppressor enable: " + mNoiseSuppressor.getEnabled());
			}
		}
		catch (Exception e) {
			log.e(TAG, "NoiseSuppressor exception: " + e);
		}

		// 回声消除
		try {
			log.d(TAG, "AcousticEchoCanceler isAvailable: " + AcousticEchoCanceler.isAvailable());
			if (AcousticEchoCanceler.isAvailable()) {
				mAcousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId);
				mAcousticEchoCanceler.setEnabled(enable);
				log.d(TAG, "AcousticEchoCanceler enable: " + mAcousticEchoCanceler.getEnabled());
			}
		}
		catch (Exception e) {
			log.e(TAG, "AcousticEchoCanceler exception: " + e);
		}
	}

	/**
	 * 录音线程
	 */
	private class recordRunnable implements Runnable {
		public void run() {
			int length = 0;
			byte[] sample = new byte[mAudioBufferSize];

			// 启动录音
			log.d(TAG, "start record");
			mAudioRecord.startRecording();

			while (mIsOpened) {
				length = mAudioRecord.read(sample, 0, sample.length);
				log.d(TAG, "AudioRecord read len: " + length);

				if (length > 0) {
					if (mRecordListener != null) {
						mRecordListener.onRecord(sample, 0, length);
					}
				}
			}

			// 停止录音
			mAudioRecord.stop();
			log.d(TAG, "stop record");
		}
	}

	public interface RecordListener {
		public void onRecord(byte[] sample, int offset, int length);
	}
}
