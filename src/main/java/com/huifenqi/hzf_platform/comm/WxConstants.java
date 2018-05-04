/** 
 * Project Name: activity_project 
 * File Name: WxConstants.java 
 * Package Name: com.huifenqi.activity.comm 
 * Date: 2016年3月24日下午2:20:58 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */  
package com.huifenqi.hzf_platform.comm;

/** 
 * ClassName: WxConstants
 * date: 2016年3月24日 下午2:20:58
 * Description: 微信常量类
 * 
 * @author xiaozhan 
 * @version  
 * @since JDK 1.8 
 */
public class WxConstants {
	
    public static final class wx {
    	
    	//地推二维码的sence id
    	public static final String WX_PUSHED_SENCEID = "10011";
    	
    	//拉钩活动的sence id
    	public static final String WX_LAGOU_SENCEID = "10000";
    	
    	
    	//拉钩活动的来源sence id1
    	public static final String WX_PUSHED_SENCEID1 = "10005";
    	
    	//拉钩活动的来源sence id2
    	public static final String WX_PUSHED_SENCEID2 = "10008";
    	
    	//签约的sence_id
    	public static final String WX_SIGNED_SENCEID = "20001";
    	
    	//C端调研的sence_id
    	public static final String WX_SIGNED_C_SURVEY = "34";
    	
    	//招聘渠道
    	public static final String WX_ZHAOPIN_SENCEID = "10006";
    }
    
    public static final class WxTempMsgStatus {
    	
    	//消息响应成功
    	public final static String MSG_STATUS_SUCCESS = "success";
    
    }
    
	// 消息状态
	public static class MsgStatus {
		// 失败
		public static final int MSG_STATUS_FAILED = 1;

		// 成功
		public static final int MSG_STATUS_SUCCESS = 2;
		
		// 未发送
		public static final int MSG_STATUS_UN_SEND = 3;
		
		// 已发送
		public static final int MSG_STATUS_SEND_START = 4;
	}
}
