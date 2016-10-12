package com.mirsery.socket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksMessage;
import io.netty.util.CharsetUtil;

public class SocksProxyMessageEncoder extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if (msg instanceof SocksMessage) {
			System.out.println("Scoket Message - - - ");
			
			ByteBuf out =  Unpooled.buffer(100);

			((SocksMessage) msg).encodeAsByteBuf(out);

			System.out.println("msg:" + msg);

			if (msg instanceof SocksCmdResponse)
				System.out.println(
						"msg:" + ((SocksCmdResponse) msg).cmdStatus().name() + "," + ((SocksCmdResponse) msg).host());
			System.out.println("out:" + out.toString(CharsetUtil.US_ASCII));

			ctx.writeAndFlush(out);
			out.release();
		} else {
			System.out.println("socket message encode - - -" + msg);
			ctx.writeAndFlush(msg);
		}
	}

}
