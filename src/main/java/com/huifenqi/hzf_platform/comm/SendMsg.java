/**
 *SendMsgTODO$
 *
 *
 *
 */
package com.huifenqi.hzf_platform.comm;

import javax.jms.Destination;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import com.huifenqi.hzf_platform.context.Constants;

/** 
* @author yzj E-mail:yangzhijiang@huizhaofang.com 
* @version 创建时间：2016年6月17日 下午5:38:54 
* 类说明 
*/
@Component
public class SendMsg {

	private static final Log log = LogFactory.getLog(SendMsg.class);
	
	@Value("${huizhaofang.topic.msg.callback}")
	private String templateDestination;
	
	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;
	
	
	public void sendMsg(String destinationName, int topicType, String msg){
		log.debug("The send message is : destinationName=[" + destinationName + "] topicType=["
				+ topicType + "]" + " msg=[" + msg + "]");
		//对输入参数进行校验
		if (StringUtils.isBlank(destinationName) || StringUtils.isBlank(msg)) {
			log.info("destinationName is null or msg is null!");
			return ;
		}
		
		try {
			Destination destination = getDestination(destinationName, topicType);
			jmsMessagingTemplate.convertAndSend(destination, msg);
		} catch (Throwable e) {
			log.error(e.toString());
			//构造错误的返回结果
		}
		return ;
	}
	
	/**
	 * 发送模板消息
	 * @param templateId
	 * @param toUsers
	 * @param userType
	 * @param Properties
	 * @return
	 */
	public void sendTemplateMsg(String msg) {
		sendMsg(templateDestination, Constants.JmsConstants.DESTINATION_QUEUE, msg);
	}
	
	/**
	 * 获取对应的目的地
	 * @param destinationName
	 * @param topicType
	 * @return
	 */
	private Destination getDestination(String destinationName, int topicType) {
		//如果不存在则放入新的目地
		Destination destination = null;
		switch(topicType) {
		case Constants.JmsConstants.DESTINATION_QUEUE:
			destination = new ActiveMQQueue(destinationName);
			break;
		case Constants.JmsConstants.DESTINATION_TOPIC:
			destination = new ActiveMQTopic(destinationName);
			break;
		}
		return destination;
	}
}
