package org.xinc.mqtt.broker;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.xinc.function.InceptionException;
import org.xinc.mqtt.MqttInception;
import org.xinc.mqtt.client.MqttClient;
import org.xinc.mqtt.client.MqttClientProperty;

import java.util.HashMap;


@Slf4j
public class MqttBrokerHandler extends SimpleChannelInboundHandler<Object> {

    MqttInception mqttInception = new MqttInception();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端已经离线 返还 http 句柄");
        MqttClient upstreamClient = (MqttClient) ctx.channel().attr(AttributeKey.valueOf("mqtt_connect")).get();
        upstreamClient.close();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        log.info("客户端已经上线 启动后端mqtt链接 句柄");
        HashMap<String, Object> config = new HashMap<>();
        config.put("downStream",ctx.channel());
        MqttClient upstreamClient = new MqttClient(new MqttClientProperty("/application-client.properties"),ctx.channel());
        ctx.channel().attr(AttributeKey.valueOf("mqtt_connect")).set(upstreamClient);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        MqttClient upstreamClient = (MqttClient) ctx.channel().attr(AttributeKey.valueOf("mqtt_connect")).get();
        try {
            mqttInception.checkRule(msg);
        } catch (InceptionException e) {
            e.printStackTrace();
            return;
        }
        upstreamClient.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}