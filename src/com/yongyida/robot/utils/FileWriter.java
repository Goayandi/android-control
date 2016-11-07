package com.yongyida.robot.utils;

import net.surina.soundtouch.log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {
	private static final String TAG = "FileWriter";

	private String mFileName;
	private FileOutputStream mFile;

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		mFileName = fileName;
	}

	public boolean isOpened() {
		return (mFile != null);
	}

	public void open() {
		log.d(TAG, "open()");

		if (isOpened()) {
			close();
		}

		try {
			mFile = new FileOutputStream(mFileName);
		}
		catch (FileNotFoundException e) {
			log.e(TAG, "Open file error");
		}
	}

	public void close() {
		log.d(TAG, "close()");

		if (mFile != null) {
			try {
				mFile.close();
			}
			catch (IOException e) {
				log.e(TAG, "File close error: " + e);
			}
		}
	}

	public void write(byte[] data, int offset, int length) {
		if (mFile != null) {
			try {
				mFile.write(data, offset, length);
			}
			catch (IOException e) {
				log.e(TAG, "Write error: " + e);
			}
		}
	}
}
