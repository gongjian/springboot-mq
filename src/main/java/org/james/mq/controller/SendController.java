package org.james.mq.controller;

import java.util.UUID;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rabbit")
public class SendController {
	@Autowired
    private RabbitTemplate rabbitTemplate;

	/**
     *  测试Direct模式.
     *
     * @return the response entity
     */
    @RequestMapping("/direct")
    public ResponseEntity<String> direct() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE", "DIRECT_ROUTING_KEY", "123", correlationData);
        return ResponseEntity.status(HttpStatus.OK).body("direct OK");
    }
    
    /**
     *  测试广播模式.
     *
     * @return the response entity
     */
    @RequestMapping("/fanout")
    public ResponseEntity<String> send() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend("FANOUT_EXCHANGE", "", "234", correlationData);
        return ResponseEntity.status(HttpStatus.OK).body("Send OK");
    }

    
    
    /**
     * 测试死信队列.
     *
     * @param p the p
     * @return the response entity
     */
    @RequestMapping("/dead")
    public ResponseEntity<String> deadLetter() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        // 声明消息处理器  这个对消息进行处理  可以设置一些参数   对消息进行一些定制化处理   我们这里  来设置消息的编码  以及消息的过期时间  因为在.net 以及其他版本过期时间不一致   这里的时间毫秒值 为字符串
        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            // 设置编码
            messageProperties.setContentEncoding("utf-8");
            // 设置过期时间10*1000毫秒
            messageProperties.setExpiration("10000");
            return message;
        };
        // 向DL_QUEUE 发送消息  10*1000毫秒后过期 形成死信
        rabbitTemplate.convertAndSend("DL_EXCHANGE", "DL_KEY", "888", messagePostProcessor, correlationData);
        return ResponseEntity.status(HttpStatus.OK).body("DL OK");
    }
}
