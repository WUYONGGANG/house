/** 
 * Project Name: test_20160328 
 * File Name: WeixinRequestHandller.java 
 * Package Name: com.huifenqi.usercomm.app.weixin 
 * Date: 2016年3月30日下午4:59:14 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.hzf_platform.handler;

import com.google.gson.JsonObject;
import com.huifenqi.hzf_platform.comm.BaseRequestHandler;
import com.huifenqi.hzf_platform.comm.RedisClient;
import com.huifenqi.hzf_platform.context.Constants;
import com.huifenqi.hzf_platform.context.WeixinConstants;
import com.huifenqi.hzf_platform.context.exception.BaseException;
import com.huifenqi.hzf_platform.context.exception.ErrorMsgCode;
import com.huifenqi.hzf_platform.context.response.Responses;
import com.huifenqi.hzf_platform.service.FileService;
import com.huifenqi.hzf_platform.utils.LogUtil;
import com.huifenqi.hzf_platform.utils.MessageUtil;
import com.huifenqi.hzf_platform.utils.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: WeixinRequestHandller date: 2016年3月30日 下午4:59:14 Description:
 * 
 * @author arison
 * @version
 * @since JDK 1.8
 */
@Service
public class WeixinRequestHandler extends BaseRequestHandler {

	@Autowired
	private WeiXinKits weiXinKits;
	
	@Autowired
    private RedisClient redisClient;
	
	@Autowired
    private FileService fileService;

	/**
	 * 获取微信JSDK的授权信息
	 * 
	 * @return
	 */
	public Responses reqWxjsdkAuth() {
		String url = getDefaultStringParam("target_url", null);
        JsonObject retJo = new JsonObject();
		try {
			if (url == null) {
				url = (String) getRequest().getHttpServletRequest().getHeader("Referer");
			}
			if (url == null) {
				 throw new BaseException(ErrorMsgCode.ERRCODE_WXJSDK_AUTH_FAILED, "获取授权信息失败");
			}
			Map<String, String> authInfos = weiXinKits.reqJsSDKAuth(url);
			for (String key : authInfos.keySet()) {
				retJo.addProperty(key, authInfos.get(key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new  BaseException(ErrorMsgCode.ERRCODE_WXJSDK_AUTH_FAILED, "获取授权信息失败");
		}

		Responses responses = new Responses();
		responses.getMeta().setErrorCode(ErrorMsgCode.ERROR_MSG_OK);
		responses.setBody(retJo);
		return responses;
	}

	/**
	 * 获取微信接口调用凭据
	 * 
	 * @return
	 */
	public Responses reqWxAccessToken() {
		JsonObject jsonObject = new JsonObject();
		try {
			String wxAccessToken = weiXinKits.reqWxAccessToken();
			jsonObject.addProperty("access_token", wxAccessToken);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new  BaseException(ErrorMsgCode.ERRCODE_WX_GET_ACCESS_TOKEN_FAIL, "获取授权信息失败");
		}
		Responses responses = new Responses();
		responses.getMeta().setErrorCode(ErrorMsgCode.ERROR_MSG_OK);
		responses.setBody(jsonObject);
		return responses;
	}
	
	/**
	 * @Title: reqWxQrCode
	 * @Description: 获取微信二维码
	 * @return Responses
	 * @author 叶东明
	 * @dateTime 2018年3月13日 下午6:14:43
	 */
	public Responses reqWxQrCode() {
        String sceneParam = getStringParam("senceParam", "二维码场景值");
        try {
            // 使用临时二维码时，缓存id与场景值关系
            Long nextId = weiXinKits.reqNextTempQrCodeSceneId();
            if (nextId == null) {
                logger.error("临时二维码场景id为空");
                throw new BaseException(ErrorMsgCode.ERRCODE_QR_GENERATE_FAIL, "生成微信二维码失败");
            }
            logger.info(String.format("临时二维码场景id:%d", nextId));

            // 保存id到场景值的关系
            String idKey = weiXinKits.getTempQrCodeIdRedisKey(nextId);
            logger.info("二维码场景值的关系：======"+idKey);
            int expireSeconds = WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_EXPIRE_SECONDS_DEFAULT;
            redisClient.set(idKey, sceneParam, expireSeconds, TimeUnit.SECONDS);

            // 临时二维码为数字类型
            int sceneType = Constants.WeixinQrCode.SENCE_TYPE_NUMBER;

            // 1.发送创建二维码Ticket请求
            String sceneIdStr = String.valueOf(nextId);
            String ticket = weiXinKits.reqTempWeixinQrcodeTicket(sceneType, sceneIdStr);
            logger.info("二维码Ticket值：======"+ticket);
            if (StringUtil.isEmpty(ticket)) {
                logger.error(String.format("failed to get permanent qrcode ticket, sceneType:%s, sceneParam:%s",
                        sceneType, sceneIdStr));
                throw new BaseException(ErrorMsgCode.ERRCODE_QR_GENERATE_FAIL, "生成微信二维码失败");
            }

            // 2.通过ticket换取二维码
            String ticketUrl = String.format(WeixinConstants.WX_GET_QRCODE_URL_FORMAT, ticket);
            String cacheFileNamePrex = String.valueOf(sceneParam + System.currentTimeMillis());
            String cacheFilePath = fileService.downFileFromWx(ticketUrl, cacheFileNamePrex);

            // 3.把本机的文件上传到文件服务器
            String filePath = fileService.uploadFile(cacheFilePath, 4, "");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("path", filePath);

            Responses responses = new Responses();
            responses.getMeta().setErrorCode(ErrorMsgCode.ERROR_MSG_OK);
            responses.setBody(jsonObject);
            return responses;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BaseException(ErrorMsgCode.ERRCODE_QR_GENERATE_FAIL, "生成微信二维码失败");
        }
    }
	
	/**
     * 向微信发送数据
     * 
     * @param response
     * @param msg
     */
    public void wxResponse(HttpServletResponse response, String msg) {
        PrintWriter out = null;
        try {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();
            out.print(msg);
            out.flush();
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
    }
	
	public String reqWxMsg(HttpServletRequest request) {
	    /**
         * xml请求解析
         */
        Map<String, String> requestMap = null;
        try {
            requestMap = MessageUtil.parseXml(request);
        } catch (Exception e) {
            logger.error("XML request parsing errors:", e);
        }

        /**
         * 发送方帐号（open_id）
         */
        String fromUserName = requestMap.get("FromUserName");
        
        /**
         * 公众帐号
         */
        String toUserName = requestMap.get("ToUserName");
        
        /**
         * 消息类型
         */
        String msgType = requestMap.get("MsgType");
        
        /**
         * 消息创建时间 （整型）
         */
        String CreateTime = requestMap.get("CreateTime");
        
        /**
         * 事件类型
         */
        String event = requestMap.get("Event");
        
        /**
         * 事件KEY值
         */
        String eventKey = requestMap.getOrDefault("EventKey", "None");
        if (eventKey == "None") {
            eventKey = "";
        }
        logger.info(LogUtil.formatLog(String.format("[REQUEST]Request parameteters: %s", requestMap)));
        if (MessageUtil.REQ_MESSAGE_TYPE_EVENT.equals(msgType)) {
            String replyEventMsg = weiXinKits.handleEventMsg(request, toUserName, fromUserName, msgType, event, requestMap);
            logger.info("replyEventMsg==========="+replyEventMsg);
            return replyEventMsg;
        } else {
            return "";
        }
	}
	
}
