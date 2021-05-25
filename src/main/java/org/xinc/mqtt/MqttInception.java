package org.xinc.mqtt;


import lombok.extern.slf4j.Slf4j;
import org.xinc.function.Inception;
import org.xinc.function.InceptionException;


@Slf4j
public class MqttInception implements Inception {
    @Override
    public void checkRule(Object source) throws InceptionException {
        System.out.println("mqtt 请求审核 Inception");
    }
}
