/** 
* Project Name: trunk 
* File Name: WeixinConstants.java 
* Package Name: com.huifenqi.usercomm.comm.weixin 
* Date: 2016年3月21日上午9:55:41 
* Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
* 
*/
package com.huifenqi.hzf_platform.context;

/**
 * ClassName: WeixinConstants date: 2016年3月21日 上午9:55:41 Description:
 * 
 * @author maoxinwu
 * @version
 * @since JDK 1.8
 */
public class WeixinConstants {

	public static final String WX_AUTH_REDIRECT = "http://%s/wx/wxindexredirect/";

	public static final String WX_INDEX_PAGE = "http://%s/hzfWechat/";
	
	// 获取微信二维码地址
    public static final String WX_GET_QRCODE_URL_FORMAT = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s";
	
	// 获取微信二维码ticket地址
    public static final String WX_GET_QRCODE_TICKET_URL_FORMAT = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
	
	// 获取临时微信二维码ticket, 过期时间默认值
    public static final int WX_GET_QRCODE_TICKET_REQ_JSON_KEY_EXPIRE_SECONDS_DEFAULT = 30 * 24 * 3600;
    
    // 获取微信二维码ticket JSON key:action_name
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_NAME = "action_name";
    
    // 获取永久微信二维码ticket, action_name, 数值型
    public static final String WX_GET_QRCODE_TICKET_ACTION_NAME_PERM_NUM = "QR_LIMIT_SCENE";
    
    // 获取微信二维码ticket,JSON key:scene
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE = "scene";
    
    // 获取微信二维码ticket,JSON key:sceneId
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID = "scene_id";
    
    // 获取微信二维码ticket,JSON key:sceneStr
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_STR = "scene_str";
    
    // 获取永久微信二维码ticket, action_name，字符型
    public static final String WX_GET_QRCODE_TICKET_ACTION_NAME_PERM_STR = "QR_LIMIT_STR_SCENE";
    
    // 获取微信二维码ticket,JSON key:action_info
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_INFO = "action_info";
    
    // 获取微信二维码ticket,JSON key:expire_seconds
    public static final String WX_GET_QRCODE_TICKET_REQ_JSON_KEY_EXPIRE_SECONDS = "expire_seconds";
    
    // 获取临时微信二维码ticket, action_name
    public static final String WX_GET_QRCODE_TICKET_ACTION_NAME_TEMP = "QR_SCENE";
    
    // 永久二维码SceneId最小值
    public static final int WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MIN = 1;
    
    // 永久二维码SceneId最大值
    public static final int WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MAX = 100000;
}
