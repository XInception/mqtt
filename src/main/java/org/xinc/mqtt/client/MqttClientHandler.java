package org.xinc.mqtt.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MqttClientHandler extends ChannelInboundHandlerAdapter {

    Channel downStreamChanel = null;

    MqttClientProperty property = null;

    public MqttClientHandler(Channel downStreamChanel, MqttClientProperty property) {
        this.downStreamChanel = downStreamChanel;
        this.property = property;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        System.out.println("转发数据给前端");
        System.out.println(msg);
        downStreamChanel.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.print("发生异常: ");
        cause.printStackTrace(System.err);
        ctx.close();
    }
}
