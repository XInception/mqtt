package org.xinc.mqtt;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.xinc.mqtt.client.MqttClient;
import org.xinc.mqtt.client.MqttClientProperty;

import java.util.Map;

@Slf4j
public class MqttUpstreamPool extends BaseKeyedPooledObjectFactory<Map<String, Object>, MqttClient> {

    @Override
    public MqttClient create(Map<String, Object> stringObjectMap) throws Exception {
        log.info("获取客户端");
        return new MqttClient(new MqttClientProperty("/application-client.properties"),(Channel) stringObjectMap.get("downStream"));
    }

    @Override
    public PooledObject<MqttClient> wrap(MqttClient client) {
        return new DefaultPooledObject<>(client);
    }
}
