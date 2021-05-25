package org.xinc.mqtt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


@Slf4j
public class MqttClient implements Closeable {


    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    MqttClientProperty property = null;

    Channel downstream;

    Channel upstreamChannel;

    Queue<Object> msgs = new LinkedList<>();

    public MqttClient() {

    }

    public MqttClient(MqttClientProperty mqttClientProperty, Channel downStream) {
        downstream = downStream;
        property = mqttClientProperty;
        start(mqttClientProperty, downStream);
    }

    public void start(MqttClientProperty mqttClientProperty, Channel channel) {
        downstream = channel;
        property = mqttClientProperty;
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                        new LoggingHandler(),
                        MqttEncoder.INSTANCE,
                        new MqttDecoder(),
                        new MqttClientHandler(channel,mqttClientProperty)
                );
            }
        });
        var cf = bootstrap.connect(property.server, property.port);

        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("mqtt 服务器连接成功");
                Thread t= new Thread(this::consume);
                t.start();
            }
        });
        try {
            cf.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!cf.isSuccess()) {
            throw new RuntimeException(cf.cause());
        }
        upstreamChannel = cf.channel();
    }

    public void consume() {
        log.info("mqtt连接建立完成");
        while (true) {
            //写入后端
            MsgQueue msgQueue = (MsgQueue) msgs.poll();
            if (msgQueue == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if ("write".equals(msgQueue.op)) {
//                System.out.println("------>" + msgQueue.op);
//                System.out.println("------>" + msgQueue.payload);
                upstreamChannel.write(msgQueue.payload);
            } else if("writeAndFlush".equals(msgQueue.op)) {
                upstreamChannel.writeAndFlush(msgQueue.payload);
            }else {
                upstreamChannel.flush();
            }
        }
    }

    public void write(Object msg) {
        msgs.add(new MsgQueue("write", msg));
    }

    public void writeAndFlush(Object msg) {
        msgs.add(new MsgQueue("writeAndFlush", msg));
    }

    public void flush() {
        System.out.println("发送消息1"+System.currentTimeMillis());
        msgs.add(new MsgQueue("flush"));
    }

    @Override
    public void close() {
        log.info("断开与后端服务器连接");
        try {
            upstreamChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();

            eventLoopGroup.shutdownGracefully();
        }
    }

    static class MsgQueue {
        String op;
        Object payload;

        public MsgQueue(String op) {
            this.op = op;
        }

        public MsgQueue(String op, Object payload) {
            this.op = op;
            this.payload = payload;
        }
    }

}
