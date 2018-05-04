/** 
* Project Name: trunk 
* File Name: MimeUtils.java 
* Package Name: com.huifenqi.usercomm.utils 
* Date: 2016年3月21日下午3:30:48 
* Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
* 
*/
package com.huifenqi.hzf_platform.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * ClassName: MimeUtils date: 2016年3月21日 下午3:30:48 Description:
 * 
 * @author maoxinwu
 * @version
 * @since JDK 1.8
 */
public class MimeUtils {

	private static final Log logger = LogFactory.getLog(MimeUtils.class);

	/**
	 * 根据ContentType获取扩展名
	 * 
	 * @param contentType
	 * @return
	 */
	public static String getExtension(String contentType) {

		if (StringUtil.isEmpty(contentType)) {
			return null;
		}

		// "image/jpg"类型的特殊处理，tika库不识别，强制转换下
		String contentTypeToProcess = contentType;
		if (contentTypeToProcess.equals("image/jpg")) {
			contentTypeToProcess = "image/jpeg";
		}

		String extension = null;

		MimeTypes types = MimeTypes.getDefaultMimeTypes();
		MimeType type = null;
		try {
			type = types.forName(contentTypeToProcess);
		} catch (MimeTypeException e) {
			logger.error("failed to get mime type from contentType, contentType:" + contentTypeToProcess + ", "
					+ e.getLocalizedMessage());
		}

		if (type != null) {
			extension = type.getExtension();
		}

		return extension;

	}

}
