package com.mirsery.socket.handler;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.CharsetUtil;

public class SocksServerConnectHandler extends ChannelInboundHandlerAdapter {
	private Channel inboundChannel;
	private volatile Channel outboundChannel;
	private static final String name = "SOCKS_SERVER_CONNECT_HANDLER";
	
	final Object trafficLock = new Object();
	
	public static String getName() {
		return name;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final SocksCmdRequest socksCmdRequest = (SocksCmdRequest) msg;
		System.out.println("socksCmdRequest : " + socksCmdRequest.port());

		inboundChannel = ctx.channel();
		ctx.channel().config().setAutoRead(false);

		Bootstrap worker = new Bootstrap();
		worker.group(new NioEventLoopGroup());
		
		worker.channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				System.out.println("inboundChannel:"+inboundChannel);
				ch.pipeline().addLast("outboundChannel",new OutboundHandler(inboundChannel, "out"));
			}
		}).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
		
		System.out.println("host:"+socksCmdRequest.host());

		ChannelFuture future = worker.connect(new InetSocketAddress(socksCmdRequest.host(), socksCmdRequest.port()));

		outboundChannel = future.channel();
		ctx.pipeline().remove(getName());

		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()){
					System.out.println("success outbound - - -");
					System.out.println("inboundChannel:"+inboundChannel);
					System.out.println("outboundChannel:"+outboundChannel);
					inboundChannel.pipeline().addLast("inboundChannel", new OutboundHandler(outboundChannel, "in"));
					inboundChannel.write(
							new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()));
					inboundChannel.flush();
					inboundChannel.config().setAutoRead(true);
				}else{
					System.out.println("outbound - - -");
					inboundChannel.write(
							new SocksCmdResponse(SocksCmdStatus.FAILURE, socksCmdRequest.addressType()));
					inboundChannel.close();
				}
			}
		});
	}

	private class OutboundHandler extends ChannelInboundHandlerAdapter {
		
		private final Channel inboundChannel;

		OutboundHandler(Channel inboundChannel, String name) {
			this.inboundChannel = inboundChannel;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			synchronized (trafficLock) {
				System.out.println("original channel:"+ctx.channel());
				System.out.println("msg:"+((ByteBuf)msg).toString(CharsetUtil.US_ASCII));
				System.out.println("channel:"+inboundChannel);
				inboundChannel.writeAndFlush(msg);
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			closeOnFlush(ctx.channel());
		}
	}
	
	static void closeOnFlush(Channel ch) {
		if (ch.disconnect() != null) {
			ch.write(EmptyByteBuf.class).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
