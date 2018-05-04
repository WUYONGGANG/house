package com.huifenqi.hzf_platform.handler;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.huifenqi.hzf_platform.comm.RedisClient;
import com.huifenqi.hzf_platform.comm.SendMsg;
import com.huifenqi.hzf_platform.comm.WxConstants;
import com.huifenqi.hzf_platform.context.Constants;
import com.huifenqi.hzf_platform.context.WeixinConstants;
import com.huifenqi.hzf_platform.utils.Configuration;
import com.huifenqi.hzf_platform.utils.GsonUtil;
import com.huifenqi.hzf_platform.utils.HttpUtil;
import com.huifenqi.hzf_platform.utils.LogUtil;
import com.huifenqi.hzf_platform.utils.MessageUtil;
import com.huifenqi.hzf_platform.utils.StringUtil;
import com.huifenqi.hzf_platform.utils.WeiXinXml;

/**
 * Created by stargaer on 2015/11/15.
 */
@Component
public class WeiXinKits {

	private static Logger logger = LoggerFactory.getLogger(WeiXinKits.class);

	public static final Set<String> WX_CHANNEL_CONF = new HashSet<>();

	public static final String AUTH_GET_CODE = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect";

	public static final String AUTH_GET_ACCESSTOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";

	// 获取微信接口凭证的地址
	public static final String AUTH_GET_API_ACCESSTOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

	// 获取微信接口的jsapi_ticket
	public static final String AUTH_GET_JSAPI_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";

	// 从微信下载文件
	public static final String WX_FILE_DOWNLOAD = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";

	// 授权时间,1个小时有效
	public static final long AUTH_TIMEOUT = 1 * 60 * 60 * 1000;

	public static final String WX_API_ACCESSTOKEN_PREFIX = "hzfwx.api.accesstoken";

	public static final String WX_JS_TICKET_PREFIX = "wx.jsapi.ticket";

	@Autowired
	private Configuration configuration;

	@Autowired
	private RedisClient redisClient;
	
	@Autowired
    private SendMsg sendMsg;

	// 获取token的锁
	private Object accecctokenSysnc = new Object();

	// 获取ticket的锁
	private Object ticketSync = new Object();
	
	// 获取临时二维码场景Id的锁
    private Object qrcodeIdSysnc = new Object();
    
	/**
	 * 微信公众号授权信息
	 */
	public static class WeiXinPubAuth {
		public String unionId;
		public String openId;
	}


	public static final String WX_HZF = "HZF";

	static {
		// 目前仅支持会分期公众号
		WX_CHANNEL_CONF.add(WX_HZF);
	}

	/**
	 * 获取code，该接口返回的url必须重定向
	 *
	 * @param channel
	 * @param targetUrl
	 * @return
	 * @throws Exception
	 */
	public String createGetCodeUrl(String channel, String targetUrl) throws Exception {
		if (!WX_CHANNEL_CONF.contains(channel)) {
			throw new Exception("不存在的公众号：" + channel + "，可选的值为：" + WX_CHANNEL_CONF);
		}

		String encodedTargetUrl = URLEncoder.encode(targetUrl, "UTF-8");

		return String.format(AUTH_GET_CODE, configuration.hzfAppid, encodedTargetUrl, channel);
	}

	/**
	 * 获取微信对应渠道的unionid和openid
	 *
	 * @param code
	 * @param channel
	 * @return
	 */
	public WeiXinPubAuth requestAuth(String code, String channel) throws Exception {

		if (!WX_CHANNEL_CONF.contains(channel)) {
			throw new Exception("不存在的公众号：" + channel + "，可选的值为：" + WX_CHANNEL_CONF);
		}

		Map<String, String> params = new HashMap<>();
		params.put("appid", configuration.hzfAppid);
		params.put("secret", configuration.hzfSecret);
		params.put("code", code);
		params.put("grant_type", "authorization_code");

		String response = HttpUtil.get(AUTH_GET_ACCESSTOKEN, params);
		Map<String, Object> retMap = GsonUtil.buildGson().fromJson(response, new TypeToken<Map<String, Object>>() {
		}.getType());

		String openid = (String) retMap.get("openid");
		if (StringUtil.isEmpty(openid)) {
			throw new Exception("failed to get openid");
		}

		WeiXinPubAuth pubAuth = new WeiXinPubAuth();
		pubAuth.unionId = (String) retMap.get("unionid");
		pubAuth.openId = (String) retMap.get("openid");

		return pubAuth;
	}

	/**
	 * 获取js的授权信息
	 * 
	 * @return
	 */
	public Map<String, String> reqJsSDKAuth(String url) throws Exception {
		String token = reqWxAccessToken();
		if (StringUtil.isBlank(token)) {
			throw new Exception("微信接口授权失败");
		}
		String ticket = reqWxApiTicket(token);
		if (StringUtil.isBlank(ticket)) {
			throw new Exception("微信接口授权失败");
		}
		String signatureSeed = "jsapi_ticket=%s&noncestr=%s&timestamp=%s&url=%s";
		String noncestr = generateNoncestr();
		//String timestamp = String.valueOf(System.currentTimeMillis());
		String timestamp = String.valueOf(System.currentTimeMillis()/1000);
		//add by arison
		url=java.net.URLDecoder.decode(url,"UTF-8");
		String tempS = String.format(signatureSeed, ticket, noncestr, timestamp, url);
		logger.debug("JS API 签名URL" + tempS);
		String signature = DigestUtils.sha1Hex(tempS);
		Map<String, String> result = new HashMap<>();
		result.put("timestamp", timestamp);
		result.put("nonceStr", noncestr);
		result.put("signature", signature);
		result.put("appId", configuration.hzfAppid);
		logger.debug("JS API 签名信息" + result.toString());
		return result;
	}

	/**
	 * 获取微信接口访问授权access_token
	 * 
	 * @return
	 */
	public String reqWxAccessToken() throws Exception {
		// 先从内存数据库里面查询
		String token = redisClient.get(WX_API_ACCESSTOKEN_PREFIX)==null ?"":redisClient.get(WX_API_ACCESSTOKEN_PREFIX).toString();
		logger.info("token:========="+token);
		// 如果未获取，则需要从微信平台获取
		if (StringUtil.isRedisBlank(token)) {
			// 先获取锁
			synchronized (accecctokenSysnc) {
				// 检查是否已经获得新的token，如果有则直接返回
				String curToken = redisClient.get(WX_API_ACCESSTOKEN_PREFIX)==null ?"":redisClient.get(WX_API_ACCESSTOKEN_PREFIX).toString();
				if (!StringUtil.isRedisBlank(curToken)) {
					return curToken;
				}
				String url = String.format(AUTH_GET_API_ACCESSTOKEN, configuration.hzfAppid, configuration.hzfSecret);
				logger.info("curTokenUrl:========="+url);
				try {
					String response = HttpUtil.get(url);
					logger.info("errMsg:========="+response);
					Map<String, String> retMap = GsonUtil.buildGson().fromJson(response,
							new TypeToken<Map<String, String>>() {
							}.getType());
					token = retMap.get("access_token")==null?"":retMap.get("access_token").toString();
					if (StringUtil.isBlank(token)) {
						throw new Exception("获取Access Token失败!");
					}
					// 缓存到内存数据库,1分钟
					// int expiresIn =
					// ((Double)retMap.get("expires_in")).intValue();
					// redisClient.set(WX_API_ACCESSTOKEN_PREFIX, token,
					// expiresIn / 4 * 1000);
					//redisClient.set(WX_API_ACCESSTOKEN_PREFIX, token, 60 * 1000);
					redisClient.set(WX_API_ACCESSTOKEN_PREFIX, token, 60 * 60 * 1000);
				} catch (Exception e) {
					logger.error("获取Access Token失败:" + e.toString());
					throw new Exception("获取Access Token失败!");
				}
				try {
					reqWxApiTicket(token);
				} catch (Exception e) {
					logger.error("刷新ticket失败:" + e.toString());
				}
			}
		}
		return token;
	}

	/**
	 * 请求微信JS API的ticket
	 * 
	 * @param accessToken
	 * @return
	 * @throws Exception
	 */
	public String reqWxApiTicket(String accessToken) throws Exception {
		// 先从内存数据库里面查询
		String ticket = (String) redisClient.get(WX_JS_TICKET_PREFIX);
		// 如果未获取，则需要从微信平台获取
		if (StringUtil.isRedisBlank(ticket)) {
			// 先获取锁
			synchronized (ticketSync) {
				// 查看内存是否有最新的ticket，如果有则直接返回
				String curTicket = (String) redisClient.get(WX_JS_TICKET_PREFIX);
				if (!StringUtil.isRedisBlank(curTicket)) {
					return curTicket;
				}
				String url = String.format(AUTH_GET_JSAPI_TICKET, accessToken);
				try {
					String response = HttpUtil.get(url);

					WxTicket wxTicket = GsonUtil.buildGson().fromJson(response, WxTicket.class);
					if (0 != wxTicket.errcode) {
						throw new Exception("获取ticket失败!" + wxTicket.errmsg);
					}
					ticket = wxTicket.ticket;
					if (StringUtil.isBlank(ticket)) {
						throw new Exception("获取ticket失败!");
					}
					// 缓存到内存数据库,1分钟
					// redisClient.set(WX_JS_TICKET_PREFIX, ticket,
					// wxTicket.expiresIn / 4 * 1000);
					redisClient.set(WX_JS_TICKET_PREFIX, ticket, 60 * 1000);
				} catch (Exception e) {
					logger.error("获取ticket失败:" + e.toString());
					throw new Exception("获取ticket失败!");
				}
			}
		}
		return ticket;
	}

	/**
	 * 清空微信的访问授权和微信JS API的ticket
	 */
	public void delWxTokenAndTicket() {
		logger.info("Delete The access token and ticket from cache.");
		redisClient.delete(WX_API_ACCESSTOKEN_PREFIX);
		redisClient.delete(WX_JS_TICKET_PREFIX);
	}

	/**
	 * 随机获取一个16位的字符串，有数字和字母（大小写）组成
	 * 
	 * @return
	 */
	public String generateNoncestr() {
		Random random = new Random(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			int seed1 = random.nextInt(3);
			switch (seed1) {
			// 大写字符
			case 0:
				sb.append((char) (65 + random.nextInt(26)));
				break;
			// 小写字母
			case 1:
				sb.append((char) (97 + random.nextInt(26)));
				break;
			// 数字
			default:
				sb.append(random.nextInt(10));
				break;
			}
		}
		return sb.toString();
	}

	public String getWxDownloadApi(String mediaId) throws Exception {
		String token = reqWxAccessToken();
		return String.format(WX_FILE_DOWNLOAD, token, mediaId);
	}

	/**
	 * 验证签名
	 * 
	 * @author zangxufeng
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public boolean checkSignature(String signature, String timestamp, String nonce, String token) {
		String[] arr = new String[] { token, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		String tmpStr = DigestUtils.sha1Hex(content.toString());
		return tmpStr != null ? tmpStr.equalsIgnoreCase(signature) : false;
	}

	private Integer getIntValue(String str) {

		Integer intValue = null;
		try {
			intValue = Integer.valueOf(str);
		} catch (NumberFormatException e) {
		}
		return intValue;
	}

	class WxTicket {
		public int errcode;
		public String errmsg;
		public String ticket;
		public int expiresIn;
	}
	
	/**
     * 获取下一个临时二维码场景Id
     * 
     * @return
     */
    public Long reqNextTempQrCodeSceneId() throws Exception {

        String maxIdKey = getTempQrCodeMaxIdRedisKey();

        // 先从内存数据库里面查询
        Long nextId = redisClient.increment(maxIdKey, 1);

        // 如果Id大于限制值，重置maxId
        if (nextId > Integer.MAX_VALUE) {

            // 先获取锁
            synchronized (qrcodeIdSysnc) {

                // 检查是否已经重置，如果已重置，则返回下一个值
                Long curNextId = redisClient.increment(maxIdKey, 1);
                if (curNextId <= Integer.MAX_VALUE) {
                    return curNextId;
                }

                try {
                    long initValue = 1;
                    redisClient.set(maxIdKey, initValue);
                    nextId = initValue;
                } catch (Exception e) {
                    logger.error("重置maxId失败:" + e.toString());
                    throw new Exception("重置maxId失败");
                }
            }
        }
        return nextId;
    }
    
    /**
     * 获取临时二维码最大Id缓存Key
     * 
     * @return
     */
    public String getTempQrCodeMaxIdRedisKey() {

        String qrcodeParamPrefix = Constants.WeixinQrCode.QRCODE_PARAM_PREFIX;
        String hfqAppid = configuration.hzfAppid;
        logger.info("qrcodeParamPrefix:==="+qrcodeParamPrefix+"----hfqAppid:==="+hfqAppid);
        String maxIdKey = String.format("%s%s_maxId", qrcodeParamPrefix, hfqAppid);
        return maxIdKey;
    }
    
    /**
     * 获取临时二维码Id缓存Key
     * 
     * @return
     */
    public String getTempQrCodeIdRedisKey(Long nextId) {

        String qrcodeParamPrefix = Constants.WeixinQrCode.QRCODE_PARAM_PREFIX;
        String hfqAppid = configuration.hzfAppid;
        String idKey = String.format("%s%s.%d", qrcodeParamPrefix, hfqAppid, nextId);
        logger.info(nextId + "===qrcodeParamPrefixhfqAppid===" + idKey);
        return idKey;
    }
    
    /**
     * 
     * 获取临时二维码Ticket
     * 
     * @param sceneId
     * @return
     * @throws Exception
     */
    public String reqTempWeixinQrcodeTicket(int sceneType, String sceneParam) throws Exception {
        return reqWeixinQrcodeTicket(sceneType, sceneParam, false);
    }
    
    /**
     * 
     * 获取二维码Ticket
     * 
     * @param sceneId
     * @return
     * @throws Exception
     */
    public String reqWeixinQrcodeTicket(int sceneType, String sceneParam, boolean isPermanent) throws Exception {

        if (StringUtil.isEmpty(sceneParam)) {
            return null;
        }

        String errorMsg = validateReqWxQrCodeParams(sceneType, sceneParam, isPermanent);
        if (errorMsg != null) {
            throw new Exception(errorMsg);
        }
        logger.info("获取二维码Ticket  errorMsg:========="+errorMsg);

        String accessToken = reqWxAccessToken();
        if (StringUtil.isBlank(accessToken)) {
            logger.error("failed to get weixin access token");
            throw new Exception("failed to get weixin access token");
        }

        String reqJsonStr = getWeixinQrCodeTicketReqJson(sceneType, sceneParam, isPermanent);
        if (StringUtil.isEmpty(reqJsonStr)) {
            logger.error("failed to get weixin qrcode ticket request json string");
            throw new Exception("failed to get weixin qrcode ticket request json string");
        }

        String ticketUrl = String.format(WeixinConstants.WX_GET_QRCODE_TICKET_URL_FORMAT, accessToken);
        String responseStr = HttpUtil.post(ticketUrl, reqJsonStr);

        Map<String, Object> responseMap = GsonUtil.buildGson().fromJson(responseStr,
                new TypeToken<Map<String, Object>>() {
                }.getType());

        // 先检查是否失败
        Object errMsgObj = responseMap.get("errmsg");
        String errMsg = null;
        if (errMsgObj instanceof String) {
            errMsg = (String) errMsgObj;
        }
        if (errMsg != null) {
            throw new Exception(errMsg);
        }

        // 从响应中获取ticket
        String ticket = null;
        Object ticketObj = responseMap.get("ticket");
        if (ticketObj instanceof String) {
            ticket = (String) ticketObj;
        }

        if (StringUtil.isBlank(ticket)) {
            throw new Exception("failed to get weixin qrcode ticket from reponse, ticket is null");
        }

        return ticket;
    }
    
    /**
     * 申请二维码传入参数校验
     * 
     * @param platform
     * @param sceneType
     * @param sceneParam
     * @return
     */
    private String validateReqWxQrCodeParams(int sceneType, String sceneParam, boolean isPermanent) {

        if (isPermanent) { // 永久二维码
            if (sceneType == Constants.WeixinQrCode.SENCE_TYPE_NUMBER) { // 数值型
                Integer intValue = getIntValue(sceneParam);
                if (intValue == null) {
                    return "failed parse int value from sceneParam";
                } else if (intValue < WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MIN
                        || intValue > WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MAX) {
                    return String.format("scene_id out of range:%s-%s",
                            WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MIN,
                            WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID_PERM_MAX);
                }
            } else if (sceneType == Constants.WeixinQrCode.SENCE_TYPE_STRING) { // 字符型
                if (StringUtil.isEmpty(sceneParam)) {
                    return "sceneParam is empty";
                } else if (sceneParam.length() > 64) {
                    return "sceneParam length exceeds the limit:64";
                }
            }
        } else { // 临时二维码
            if (sceneType != Constants.WeixinQrCode.SENCE_TYPE_NUMBER) { // 非数值型
                return "sceneType must be 1 while request temporary qr code";
            }
            logger.info("nextId============"+sceneParam);
            Integer intValue = getIntValue(sceneParam);
            if (intValue == null) {
                return "failed parse int value from sceneParam";
            } else if (intValue == 0) {
                return "scene_id cannot be zero while sceneType is 1";
            }
        }

        return null;
    }
    
    /**
     * 
     * 获取申请二维码请求JSON串
     * 
     * @param sceneId
     * @param isPermanent
     * @return
     */
    public String getWeixinQrCodeTicketReqJson(int sceneType, String sceneParam, boolean isPermanent) {

        if (StringUtil.isEmpty(sceneParam)) {
            return null;
        }

        String jsonString = null;
        JsonObject jsonObject = new JsonObject();
        if (isPermanent) { // 永久二维码

            JsonObject sceneObject = new JsonObject();

            if (sceneType == Constants.WeixinQrCode.SENCE_TYPE_NUMBER) { // 数值型

                // actionName
                jsonObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_NAME,
                        WeixinConstants.WX_GET_QRCODE_TICKET_ACTION_NAME_PERM_NUM);

                // sceneId
                sceneObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID, sceneParam);

            } else if (sceneType == Constants.WeixinQrCode.SENCE_TYPE_STRING) { // 字符型
                String sceneStr = sceneParam;

                jsonObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_NAME,
                        WeixinConstants.WX_GET_QRCODE_TICKET_ACTION_NAME_PERM_STR);

                sceneObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_STR, sceneStr);
            }

            // actionInfo
            JsonObject actionInfoObject = new JsonObject();
            actionInfoObject.add(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE, sceneObject);

            jsonObject.add(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_INFO, actionInfoObject);

        } else { // 临时二维码

            // 过期时间
            jsonObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_EXPIRE_SECONDS,
                    String.valueOf(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_EXPIRE_SECONDS_DEFAULT));

            // actionName
            jsonObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_NAME,
                    WeixinConstants.WX_GET_QRCODE_TICKET_ACTION_NAME_TEMP);

            JsonObject sceneObject = new JsonObject();
            sceneObject.addProperty(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE_ID, sceneParam);

            JsonObject actionInfoObject = new JsonObject();
            actionInfoObject.add(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_SCENE, sceneObject);

            jsonObject.add(WeixinConstants.WX_GET_QRCODE_TICKET_REQ_JSON_KEY_ACTION_INFO, actionInfoObject);

        }

        // TODO 统一生成Json串方式
        jsonString = jsonObject.toString();

        return jsonString;
    }
    
    /**
     * 处理EVENT事件信息
     * 
     * @param toUserName
     * @param fromUserName
     * @param msgType
     * @param event
     * @return
     */
    public String handleEventMsg(HttpServletRequest request, String toUserName, String fromUserName, String msgType, String event,
            Map<String, String> requestMap) {
        
        if (MessageUtil.EVENT_TYPE_TEMPLATED_MSG_RESPONSE.equals(event)) {
            return handleTemplateMsgResponse(requestMap);
        } else {
            /**
             * 事件KEY值
             */
            String eventKey = requestMap.getOrDefault("EventKey", "None");
            logger.info("eventKey1:===========" + eventKey);
            if (eventKey == "None") {
                eventKey = "";
            }
            return handleEventMsg(request, toUserName, fromUserName, event, eventKey, "", msgType);
        }
    }
    
    /**
     * 处理模板消息的响应
     * @param toUserName
     * @param fromUserName
     * @param msgType
     * @param event
     * @param requestMap
     * @return
     */
    public String handleTemplateMsgResponse(Map<String, String> requestMap) {
        logger.info(LogUtil.formatLog(String.format("处理消息(MsgID=%s)", requestMap.get("MsgID"))));
        JsonObject json = new JsonObject();
        json.addProperty("MsgID", requestMap.get("MsgID"));
        json.addProperty("Status", requestMap.get("Status"));
        String callback = GsonUtil.buildGson().toJson(json);
        sendMsg.sendTemplateMsg(callback);
        return "";
    }
    
    /**
     * 处理EVENT事件信息
     * 
     * @param toUserName
     * @param fromUserName
     * @param event
     * @param eventKey
     * @return
     */
    public String handleEventMsg(HttpServletRequest request, String toUserName, String fromUserName, String event, String eventKey,
            String kfaccount, String msgType) {
        //处理数字场景二维码
        return handleLimitScene(toUserName, fromUserName, event, eventKey, kfaccount, msgType);
    }
    
    /**
     * 
     * 处理QR_LIMIT_SCENE事件
     * 
     * @param toUserName
     * @param fromUserName
     * @param event
     * @param eventKey
     * @param kfaccount
     * @param msgType
     * @return
     */
    public String handleLimitScene(String toUserName, String fromUserName, String event, String eventKey,
            String kfaccount, String msgType) {
        // 用户未关注时，进行关注后的事件推送(扫描二维码事件)
        if (event.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE) && eventKey.startsWith("qrscene_")) {
            // 记日志
            String sceneId = eventKey.substring(8);
            
            //先判断是不是签约二维码
            String replyMsg = getScanReplay(sceneId, toUserName, fromUserName);
            logger.info("用户未关注时，进行关注后的事件推送(扫描二维码事件)replyMsg:"+replyMsg);
            if (StringUtils.isBlank(replyMsg)) {
                replyMsg = replyTextMsg(toUserName, fromUserName, getCopyWriter());
            }
            
            return replyMsg;
        }
        // 用户已关注时的事件推送(扫描二维码事件)
        if (event.equals(MessageUtil.EVENT_TYPE_SCAN)) {
            String sceneId = eventKey;
            //先判断是不是签约二维码
            String replyMsg = getScanReplay(sceneId, toUserName, fromUserName);
            logger.info("用户已关注时的事件推送(扫描二维码事件)replyMsg:"+replyMsg);
            if (StringUtils.isBlank(replyMsg)) {
                 replyMsg = replyTextMsg(toUserName, fromUserName, getCopyWriter());
            }
            return replyMsg;
        }

        // 用户关注事件
        if (event.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
            String replyMsg = replyTextMsg(toUserName, fromUserName, getCopyWriter());
            return replyMsg;
        }

        return "";
    }
    
    /**
     * 回复文本信息
     * 
     * @param tousername
     * @param fromusername
     * @param content
     * @return
     */
    public String replyTextMsg(String toUserName, String fromUserName, String content) {
        String xmloutput = WeiXinXml.replyText(fromUserName, toUserName, content);
        logger.info("回复文本信息："+xmloutput);
        return xmloutput;
    }
    
    /**
     * 根据senceId获取对应的文案
     * @param senceId
     * @return
     */
    public String getCopyWriter() {
        return configuration.copywriterCommon;
    }
    
    /**
     * 获取响应值
     * @param scenceId
     * @param toUserName
     * @param fromUserName
     * @return
     */
    public String getScanReplay(String scenceId, String toUserName, String fromUserName) {
        
        String key = String.format("hzfqrcode_param_%s.%s", configuration.hzfAppid, scenceId);
        
        Object content = redisClient.get(key);
        
        logger.info(String.format("content=%s", content));
        if (content == null || StringUtils.isBlank((String)content)) {
            return null;
        }
        
        String[] sceneParam = ((String) content).split("&");
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(configuration.wxPushUrl);
        strBuf.append("cont=");
        strBuf.append(sceneParam[0]);
        strBuf.append("&phone=");
        strBuf.append(sceneParam[1]);
        strBuf.append("&status=");
        strBuf.append(sceneParam[2]);
        strBuf.append("&from=ca");
        List<Map<String, String>> newsList = createSubMsg(strBuf.toString());
        String replyMsg = replyNewsMsg(toUserName, fromUserName, newsList);
        
        return replyMsg;
    }
    
    /**
     * 构建关注公众号后发送给用户的图文消息
     * 
     * @return
     */
    public List<Map<String, String>> createSubMsg(String url) {
        List<Map<String, String>> newsList = new ArrayList<Map<String, String>>();
        Map<String, String> news = new HashMap<String, String>();
        news.put("title", configuration.imageTitle);
        news.put("description", configuration.imageDescriptions);
        news.put("picurl", configuration.imagePicurl);
        news.put("url", url);
        newsList.add(news);
        return newsList;
    }
    
    /**
     * 回复消息信息
     * 
     * @param toUserName
     * @param fromUserName
     * @param newsList
     * @return
     */
    public String replyNewsMsg(String toUserName, String fromUserName, List<Map<String, String>> newsList) {
        if (newsList.isEmpty()) {
            return "None";
        }
        String innerstr = "";
        for(int i=0;i<newsList.size();i++){
            Map<String,String> news = newsList.get(i);
            innerstr += WeiXinXml.newsMsgInnerTmpl(news);
        }       
        String xmloutput = WeiXinXml.replyNewsMsg(fromUserName, toUserName, newsList, innerstr);
        return xmloutput;
    }
    
}
