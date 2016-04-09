package com.yongyida.robot.net.helper;

import android.util.Log;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SocketHandler extends SimpleChannelHandler {

	private   SocketHandlerListener mListener;
	private   SocketHandler mSocketHandler;
	private   Channel channel;

	/**
	 * 异常处理
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		try {
			if (channel.isConnected()) {
				channel.close();
			}
			Throwable t = e.getCause();
			String message = t.getMessage();
			if (t.getClass().getSimpleName().equals("ClosedChannelException")) {
				Log.e("ClosedChannelException", message);
				return;
			}

			Log.e("exceptionCaught", message);
		} catch (Throwable t) {
			Log.e("exceptionCaught error", t.getMessage());
		}
	}
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		channel = ctx.getChannel();
		mListener.connectSuccess(ctx, e);
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		mListener.writeData(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		mListener.receiveSuccess(ctx, e);

	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelClosed(ctx, e);
		mListener.connectClose(ctx, e);
	}

	@Override
	public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.closeRequested(ctx, e);
		mListener.connectFail();
	}

	public   SocketHandler getInstance(SocketHandlerListener listener) {
		if (mSocketHandler == null) {
			mSocketHandler = new SocketHandler();
			mListener = listener;
		}
		return mSocketHandler;
	}

	public interface SocketHandlerListener {
		public void connectSuccess(ChannelHandlerContext ctx,
				ChannelStateEvent e);

		public void writeData(ChannelHandlerContext ctx, MessageEvent e);

		public void receiveSuccess(ChannelHandlerContext ctx, MessageEvent e);

		public void connectClose(ChannelHandlerContext ctx, ChannelStateEvent e);

		public void connectFail();

	}
}
