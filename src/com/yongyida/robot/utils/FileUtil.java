package com.yongyida.robot.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class FileUtil {

	public static void writefile(String path, byte[] bs, String name) {
		try {
			OutputStream outputStream = new FileOutputStream(path + "/" + name);
			outputStream.write(bs);
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 复制文件
	 * @param s   source
	 * @param t   to
	 */
	public static boolean fileChannelCopy(File s, File t) {

		FileInputStream fi = null;

		FileOutputStream fo = null;

		FileChannel in = null;

		FileChannel out = null;

		try {

			fi = new FileInputStream(s);

			fo = new FileOutputStream(t);

			in = fi.getChannel();//得到对应的文件通道

			out = fo.getChannel();//得到对应的文件通道

			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
			return true;

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				fi.close();

				in.close();

				fo.close();

				out.close();

			} catch (IOException e) {

				e.printStackTrace();

			}

		}
		return  false;
	}

	/**
	 *
	 * @param source
	 * @param to
	 * @return true :成功   false :失败
	 */
	public static boolean copyFile(File source,File to){

		int length=2097152;
		try {
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out=new FileOutputStream(to);
			byte[] buffer=new byte[length];
			while(true){
				int ins=in.read(buffer);
				if(ins==-1){
					in.close();
					out.flush();
					out.close();
					return true;
				}else {
					out.write(buffer, 0, ins);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 混淆照片的后缀名 .jpg改成.robot
	 * @param photoName
	 * @return
	 */
	public static String confusePhotoName(String photoName){
		if(photoName.endsWith(".jpg")){
			photoName = photoName.substring(0, photoName.length() - 3) + "robot";
			return photoName;
		}
		return "";
	}

	/**
	 * 还原照片的后缀名 .robot改成.jpg
	 * @param photoName
	 * @return
	 */
	public static String restorePhotoName(String photoName){
		if(photoName.endsWith(".robot")){
			photoName = photoName.substring(0, photoName.length() - 5) + "jpg";
			return photoName;
		}
		return "";
	}

	public static byte[] getContent(String filePath) throws IOException {
		File file = new File(filePath);
		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			Log.e("FileUtil", "file too big...");
			return null;
		}
		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) fileSize];
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length
				&& (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += numRead;
		}
		// 确保所有数据均被读取
		if (offset != buffer.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		fi.close();
		return buffer;
	}
}
