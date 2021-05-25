package org.xinc.mqtt.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class MqttClientTest extends MqttClientBaseTest {

    @Test
    public void testTest() throws IOException {
        MqttClient client=new MqttClient();
        client.start(new MqttClientProperty("/application-client.properties"),null);

        System.in.read();
    }

}