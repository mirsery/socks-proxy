package com.mirsery.socket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socks.SocksAuthResponse;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksRequest;

public class SocksServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		SocksRequest sockRequest = (SocksRequest) msg;
		
		System.out.println("begin handler message - - -" + sockRequest.type());
		
		ChannelPipeline pipeline = ctx.pipeline();
		System.out.println("The sockRequest type is : " + sockRequest.requestType());
		switch (sockRequest.requestType()) {
		case INIT:
			pipeline.addFirst(SocksCmdRequestDecoder.getName(), new SocksCmdRequestDecoder());
			ctx.channel().write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
			break;
		case AUTH:
			System.out.println("begin auth ----");
			pipeline.addFirst(SocksCmdRequestDecoder.getName(), new SocksCmdRequestDecoder());
			ctx.channel().write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
			break;
		case CMD:
			System.out.println("begin cmd ---");
			SocksCmdRequest sockCmd = (SocksCmdRequest) sockRequest;
			if (sockCmd.cmdType() == SocksCmdType.CONNECT) {
				System.out.println("socksCmdType : " + sockCmd.cmdType());
				pipeline.addLast("SOCKS_SERVER_CONNECT_HANDLER",new SocksServerConnectHandler());
				pipeline.remove(this);
			} else {
				ctx.channel().close();
			}
			break;
		default:
			break;
		}
		super.channelRead(ctx, msg);
	}
}
