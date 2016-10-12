package com.mirsery.socket.util;


import com.mirsery.socket.handler.SocksProxyMessageEncoder;
import com.mirsery.socket.handler.SocksServerHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;

public class SocksProxyPipelineInitializer extends ChannelInitializer<SocketChannel>{
	private ChannelPipeline pipeline;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		pipeline = ch.pipeline();
		pipeline.addLast(SocksInitRequestDecoder.getName(), new SocksInitRequestDecoder());
		pipeline.addLast("socksEncoder", new SocksProxyMessageEncoder());
		pipeline.addLast("socksServer", new SocksServerHandler());	
	}
	
}
