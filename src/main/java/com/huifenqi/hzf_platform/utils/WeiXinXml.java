package com.huifenqi.hzf_platform.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 回复包含xml的字符串
 * @author zangxufeng
 */
public class WeiXinXml {


	/**
	 * 回复文本信息
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param Content
	 * @return
	 */
	public static String replyText(String ToUserName,String FromUserName,String Content){
		
		String textMsgTmpl="<xml>" +
				"<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>" +
				"<FromUserName><![CDATA["+FromUserName+"]]></FromUserName> " +
				"<CreateTime>"+new Date().getTime()+"</CreateTime>" +
				"<MsgType><![CDATA[text]]></MsgType>" +
			//	"<Content><![CDATA["+Content+"]]></Content>" +
				"<Content>"+Content+"</Content>" +
				"</xml>";
		return textMsgTmpl;
	}

	/**
	 * 转发客服信息(kfaccount为空串)
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param msgType
	 * @return
	 */
	public static String textTp(String ToUserName,String FromUserName,String msgType){
		
		String texttp="<xml>\n" +
				"<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
				"<FromUserName><![CDATA["+FromUserName+"]]></FromUserName> \n" +
				"<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
				"<MsgType><![CDATA["+"transfer_customer_service"+"]]></MsgType>\n" +
				"</xml>";
		return texttp;
	}
	
	/**
	 * 回复文本信息(kfaccount不为空串)
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param kfaccount
	 * @param MsgType
	 * @return
	 */
	public static String textTps(String ToUserName,String FromUserName,String kfaccount,String msgType){
		
		String texttps="<xml>\n" +
				"<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
				"<FromUserName><![CDATA["+FromUserName+"]]></FromUserName> \n" +
				"<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
				"<MsgType><![CDATA["+"transfer_customer_service"+"]]></MsgType>\n" +
				"<TransInfo>\n<KfAccount><![CDATA["+kfaccount+"]]></KfAccount>\n</TransInfo>\n"+
				"</xml>";
		return texttps;
	}
	
	/**
	 * 要回复的消息信息
	 * 
	 * @param newsList
	 * @return
	 */
	public static String newsMsgInnerTmpl(Map<String,String> newsList){
		String newsMsg="<item>\n" +
		        "<Title><![CDATA["+newsList.get("title")+"]]></Title>\n" +
		        "<Description><![CDATA["+newsList.get("description")+"]]></Description>\n" +
		        "<PicUrl><![CDATA["+newsList.get("picurl")+"]]></PicUrl>\n" +
		        "<Url><![CDATA["+newsList.get("url")+"]]></Url>\n" +
		        "</item>\n";
		return newsMsg;
	}
	
	/**
	 * 回复消息信息
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param newsList
	 * @param innerstr
	 * @return
	 */
	public static String replyNewsMsg(String ToUserName, String FromUserName, List<Map<String,String>> newsList, String innerstr){
		String replyNews="<xml>\n" +
		        "<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
		        "<FromUserName><![CDATA["+FromUserName+"]]></FromUserName>\n" +
		        "<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
		        "<MsgType><![CDATA[news]]></MsgType>\n" +
		        "<ArticleCount>"+newsList.size()+"</ArticleCount>\n" +
		        "<Articles>\n" +
		        innerstr+"\n" +
		        "</Articles>\n" +
		        "</xml>";
		return replyNews;
	}
	
	/**
	 * 回复图片信息
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param mediaId
	 * @return
	 */
	public static String replyImageMsg(String ToUserName, String FromUserName, String mediaId){
		String imageMsgTmpl="<xml>\n" +
		        "<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
		        "<FromUserName><![CDATA["+FromUserName+"]]></FromUserName>\n" +
		        "<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
		        "<MsgType><![CDATA[image]]></MsgType>\n" +
		        "<Image>\n" +
		        "<MediaId><![CDATA["+mediaId+"]]></MediaId>\n" +
		        "</Image>\n" +
		        "</xml>";
		return imageMsgTmpl;
	}
	
	/**
	 * 回复视频信息
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param mediaId
	 * @return
	 */
	public static String replyVoiceMsg(String ToUserName, String FromUserName, String mediaId){
		String voiceMsgTmpl="<xml>\n" +
		        "<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
		        "<FromUserName><![CDATA["+FromUserName+"]]></FromUserName>\n" +
		        "<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
		        "<MsgType><![CDATA[voice]]></MsgType>\n" +
		        "<Voice>\n" +
		        "<MediaId><![CDATA["+mediaId+"]]></MediaId>\n" +
		        "</Voice>\n" +
		        "</xml>";
		return voiceMsgTmpl;
	}
	
	/**
	 * 回复视频信息(with title,desc)
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param mediaId
	 * @param title
	 * @param desc
	 * @return
	 */
	public static String replyVoiceMsg(String ToUserName, String FromUserName, String mediaId, String title, String desc){
		String voiceMsgTmpl="<xml>\n" +
		        "<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
		        "<FromUserName><![CDATA["+FromUserName+"]]></FromUserName>\n" +
		        "<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
		        "<MsgType><![CDATA[video]]></MsgType>\n" +
		        "<Video>\n" +
		        "<MediaId><![CDATA["+mediaId+"]]></MediaId>\n" +
		        "<Title><![CDATA["+title+"]]></Title>\n" +
		        "<Description><![CDATA["+desc+"]]></Description>\n" +
		        "</Video>\n" +
		        "</xml>";
		return voiceMsgTmpl;
	}
	
	/**
	 * 回复音乐信息
	 * 
	 * @param ToUserName
	 * @param FromUserName
	 * @param title
	 * @param desc
	 * @param musicurl
	 * @param hqmusicurl
	 * @param thumbmediaid
	 * @return
	 */
	public static String replyMusicMsg(String ToUserName, String FromUserName, String title, String desc, String musicurl, String hqmusicurl, String thumbmediaid){
		String musicMsgTmpl = "<xml>\n" +
		        "<ToUserName><![CDATA["+ToUserName+"]]></ToUserName>\n" +
		        "<FromUserName><![CDATA["+FromUserName+"]]></FromUserName>\n" +
		        "<CreateTime>"+new Date().getTime()+"</CreateTime>\n" +
		        "<MsgType><![CDATA[music]]></MsgType>\n" +
		        "<Music>\n" +
		        "<Title><![CDATA["+title+"]]></Title>\n" +
		        "<Description><![CDATA["+desc+"]]></Description>\n" +
		        "<MusicUrl><![CDATA["+musicurl+"]]></MusicUrl>\n" +
		        "<HQMusicUrl><![CDATA["+hqmusicurl+"]]></HQMusicUrl>\n" +
		        "<ThumbMediaId><![CDATA["+thumbmediaid+"]]></ThumbMediaId>\n" +
		        "</Music>\n" +
		        "</xml>";
		return musicMsgTmpl;
	}
}
