package com.yongyida.robot.utils;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class XmlUtil {

	public static String xml(InputStream input, String code) {
		try {
			XmlPullParser xmlPullParser = Xml.newPullParser();
			xmlPullParser.setInput(input, "utf-8");
			int eventtype = xmlPullParser.getEventType();
			while (eventtype != XmlPullParser.END_DOCUMENT) {
				switch (eventtype) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (code.equals("xin")) {
						if (xmlPullParser.getName().equals("version")) {

							return xmlPullParser.nextText();
						}
					} else if (code.equals("download")) {
						if (xmlPullParser.getName().equals("url")) {

							return xmlPullParser.nextText();
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				eventtype = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String xmltext(String text, String tree) {
		int start = text.indexOf(tree);
		int end = text.indexOf("/" + tree);
		String result= text.substring(start+tree.length()+1, end-1);
		Log.i("result", result);
		return result;
	}
	
	public final static String Y50B = "Y50B";
	public final static String YYD = "yyd";
	public final static String SHANTUKEJI = "shantukeji";
	public final static String CMCC = "cmcc";
	
	public static String xmlFota(InputStream input, String type, String code) {
		try {
			XmlPullParser xmlPullParser = Xml.newPullParser();
			xmlPullParser.setInput(input, "utf-8");
			boolean choosed = false; //是否进入指定节点
			int eventtype = xmlPullParser.getEventType();
			while (eventtype != XmlPullParser.END_DOCUMENT) {
				switch (eventtype) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (xmlPullParser.getName().equals(type)) {
						choosed = true;
					} else if (xmlPullParser.getName().equals(code)) {
						if (choosed) {
							return xmlPullParser.nextText();
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				eventtype = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
