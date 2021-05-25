package org.xinc.mqtt;

import org.xinc.mqtt.broker.MqttBroker;
import org.xinc.mqtt.broker.MqttServerProperty;

import java.io.IOException;

public class Main {

    private static MqttBroker server;

    public static void main(String[] args) {
        server = new MqttBroker();
        try {
            server.start(new MqttServerProperty("/application-server.properties"));
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
