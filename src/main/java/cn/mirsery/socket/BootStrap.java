package cn.mirsery.socket;

import com.mirsery.socket.util.SocksProxyPipelineInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class BootStrap {
	public void run() {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		ServerBootstrap boss = new ServerBootstrap();

		boss.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new SocksProxyPipelineInitializer()).option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
		;
		try {
			boss.bind(8080).sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new BootStrap().run();
	}
}