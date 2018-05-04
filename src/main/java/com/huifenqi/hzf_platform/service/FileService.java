/** 
 * Project Name: usercomm_project 
 * File Name: FileService.java 
 * Package Name: com.huifenqi.usercomm.service 
 * Date: 2016年1月12日下午4:11:50 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.hzf_platform.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.huifenqi.hzf_platform.configuration.FileConfiguration;
import com.huifenqi.hzf_platform.context.Constants;
import com.huifenqi.hzf_platform.context.enums.ImgTypeEnums;
import com.huifenqi.hzf_platform.context.exception.ErrorMsgCode;
import com.huifenqi.hzf_platform.handler.WeiXinKits;
import com.huifenqi.hzf_platform.utils.Configuration;
import com.huifenqi.hzf_platform.utils.FileUtils;
import com.huifenqi.hzf_platform.utils.GsonUtil;
import com.huifenqi.hzf_platform.utils.HttpUtil;
import com.huifenqi.hzf_platform.utils.LogUtils;
import com.huifenqi.hzf_platform.utils.MimeUtils;
import com.huifenqi.hzf_platform.utils.MsgUtils;
import com.huifenqi.hzf_platform.utils.OSSClientUtils;
import com.huifenqi.hzf_platform.utils.StringUtil;

/**
 * ClassName: FileService date: 2016年1月12日 下午4:11:50 Description: 文件服务类
 * 
 * @author xiaozhan
 * @version
 * @since JDK 1.8
 */
@Component
public class FileService {

	private static final Log logger = LogFactory.getLog(FileService.class);

	@Resource
	private Configuration configuration;
	
	@Resource
	private FileConfiguration fileConfiguration;
	
	@Resource
	private OSSClientUtils OSSClient;
	
	@Autowired
    private WeiXinKits weiXinKits;

	/**
	 * 下载文件
	 * 
	 * @param url
	 * @return 返回本地的URL
	 * @throws Exception
	 */
	/**
	 * public String downFile(String url) throws Exception { String savePath =
	 * downFile(url, createSavePath(), true); return savePath; }
	 */

	/**
	 * 下载文件
	 * 
	 * @param url
	 *            下载的URL
	 * @param savePath
	 *            保存路径
	 * @param isCompress
	 *            是否压缩
	 * @return 文件保存的路径
	 */
	public String downFile(String url, String sellId) throws Exception {
		// 调置http请求连接保持时间，防止大图片下载不成功。
		RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000).setStaleConnectionCheckEnabled(true).build();

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();

		try {
			RequestBuilder get = RequestBuilder.get();
			get.setUri(url);

			CloseableHttpResponse response = httpclient.execute(get.build());
			InputStream instream = null;
			try {
				HttpEntity entity = response.getEntity();
				logger.info(LogUtils.getCommLog(entity.toString()));
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != Constants.Common.HTTP_STATUS_CODE_OK) {
					String result = EntityUtils.toString(entity);
					logger.info(LogUtils.getCommLog(String.format("The result of downloading file is %s", result)));
					throw new Exception(String.format("下载文件出错%s", result));
				}
				instream = entity.getContent();
				String imgName = createFileName(FileUtils.getFileFexName(url));
				String savePath = FileUtils.saveAndUploadImage(instream, fileConfiguration.getImageStandardWidth(),
						fileConfiguration.getImageStandardheight(), OSSClient, sellId, imgName);
				
				return savePath;
			} finally {
				if (instream != null) {
					instream.close();
				}
				response.close();
			}
		} catch (Exception e) {
			logger.error(LogUtils.getCommLog("从服务器下载文件失败! url=" + url + " " + e));
			throw new Exception("从服务器下载文件失败!");
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(LogUtils.getCommLog(e.toString()));
			}
		}
	}
	/**
	 * 下载文件
	 * 
	 * @param url
	 *            下载的URL
	 * @param savePath
	 *            保存路径
	 * @param isCompress
	 *            是否压缩
	 * @return 文件保存的路径
	 */
	public String downImage(String url, String sellId) throws Exception {
		// 调置http请求连接保持时间，防止大图片下载不成功。
		RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000).setStaleConnectionCheckEnabled(true).build();

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();

		try {
			RequestBuilder get = RequestBuilder.get();
			get.setUri(url);
			logger.info("图片地址：" + url);
			CloseableHttpResponse response = httpclient.execute(get.build());
			InputStream instream = null;
			try {
				HttpEntity entity = response.getEntity();
				logger.info(LogUtils.getCommLog(entity.toString()));
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != Constants.Common.HTTP_STATUS_CODE_OK) {
					String result = EntityUtils.toString(entity);
					logger.info("http状态码：" + statusCode);
					logger.info(LogUtils.getCommLog(String.format("The result of downloading file is %s", result)));
					throw new Exception(String.format("下载图片http请求出错%s", result));
				}
				instream = entity.getContent();
				String imgName = createFileName(FileUtils.getFileFexName(url));
				String savePath = FileUtils.saveAndUploadImage(instream, fileConfiguration.getImageStandardWidth(),
						fileConfiguration.getImageStandardheight(), OSSClient, sellId, imgName);

				return savePath;
			} finally {
				if (instream != null) {
					instream.close();
				}
				response.close();
			}
		} catch (Exception e) {
			logger.error(LogUtils.getCommLog("图片下载失败! url=" + url + " " + e));
			throw new Exception("图片下载失败!");
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(LogUtils.getCommLog(e.toString()));
			}
		}
	}
	
	/**
     * 从微信服务器下载文件
     * 
     * @param url
     * @param fileName
     */
    public String downFileFromWx(String url, String fileName) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            RequestBuilder get = RequestBuilder.get();
            get.setUri(url);
            //避免出现超时，如果不配置，httpClient4.3以后默认>24小时
            get.setConfig(HttpUtil.getReqeustConfig());
            CloseableHttpResponse response = httpclient.execute(get.build());
            InputStream instream = null;
            try {
                HttpEntity entity = response.getEntity();
                String cTtype = entity.getContentType().getValue();
                if ("text/plain".equals(cTtype)) {
                    String result = EntityUtils.toString(entity);
                    logger.info(String.format("The download file result from wx is %s", result));
                    if (result.contains("errcode")) {
                        // 如果access token验证失败则清空缓存
                        if (result.contains(":40001")) {
                            weiXinKits.delWxTokenAndTicket();
                        }
                        throw new Exception("微信服务器返回错误:" + result);
                    }
                }
                String fn = getFileName(response);
                instream = entity.getContent();
                String filePath = configuration.fileCachePath + fileName + fn;
                logger.info(String.format("The download file saved path is %s", filePath));
                File file = new File(filePath);
                file.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int ch = 0;
                while ((ch = instream.read(buffer)) != -1) {
                    fout.write(buffer, 0, ch);
                }
                fout.flush();
                fout.close();
                return filePath;
            } finally {
                if (instream != null) {
                    instream.close();
                }
                response.close();
            }
        } catch (Exception e) {
            logger.error("从微信服务器下载文件失败! url=" + url + ExceptionUtils.getStackTrace(e));
            throw new Exception("从微信服务器下载文件失败!");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }
    
    /**
     * 从响应中获取文件名
     * 
     * @param response
     * @return
     */
    public static String getFileName(CloseableHttpResponse response) {
        String fileName = getFileNameFromContentDisposition(response);
        if (StringUtil.isEmpty(fileName)) {
            fileName = getFileExtensionFromContentType(response);
        }
        return fileName;
    }
    
    /**
     * 获取response header中Content-Disposition中的filename值
     * 
     * @param response
     * @return
     */
    public static String getFileNameFromContentDisposition(CloseableHttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values != null && values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = param.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filename;
    }
    
    /**
     * 获取response header中Content-Type中的文件扩展名
     * 
     * @param response
     * @return
     */
    public static String getFileExtensionFromContentType(CloseableHttpResponse response) {
        String extension = null;

        Header contentTypeHeader = response.getFirstHeader("Content-Type");
        if (contentTypeHeader == null) {
            return null;
        }

        HeaderElement[] values = contentTypeHeader.getElements();
        if (values == null || values.length != 1) {
            return null;
        }

        String name = values[0].getName();
        if (StringUtil.isNotEmpty(name)) {
            extension = MimeUtils.getExtension(name);
        }

        return extension;
    }
    
    /**
     * 上传文件
     * 
     * @param fileurl
     * @param types
     * @param entityId
     * @return
     * @throws Exception
     */
    public String uploadFile(String fileurl, int types, String entityId) throws Exception {
        return uploadFile(fileurl, types, entityId, Constants.Watermark.WATERMARK_TYPE_NO);
    }
    
    /**
     * 上传文件
     * 
     * @param fileurl
     * @param types
     * @param entityId
     * @return
     * @throws Exception
     */
    public String uploadFile(String fileurl, int types, String entityId, int watermark) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            RequestBuilder post = RequestBuilder.post();
            post.setUri(configuration.fileserver);
            FileBody bin = new FileBody(new File(fileurl));
            HttpEntity reqEntity = null;
            if (configuration.usedFastDFS) {
                reqEntity = MultipartEntityBuilder.create().addPart("file", bin).build();
                post.addParameter("watermark", watermark + "");
            } else {
                reqEntity = MultipartEntityBuilder.create().addPart("img", bin).build();
                post.addParameter("types", types + "");
                post.addParameter("id", entityId);
            }
            post.setEntity(reqEntity);
            HttpUriRequest upfile = post.build();
            logger.info(String.format("[post request] upload file(%s) to the fastdfs.", fileurl));
            CloseableHttpResponse response = httpclient.execute(upfile);
            try {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                logger.info(
                        String.format("[post response] The response result of uploading file(%s) to fastdfs is %s .",
                                fileurl, result));
                JsonObject serviceRespJo = GsonUtil.buildGson().fromJson(result, JsonObject.class);
                JsonObject statusJo = serviceRespJo.getAsJsonObject("status");
                int code = statusJo.getAsJsonPrimitive("code").getAsInt();
                if (ErrorMsgCode.ERROR_MSG_OK == code) {
                    JsonObject resultJo = serviceRespJo.getAsJsonObject("result");
                    if (configuration.usedFastDFS) {
                        return resultJo.getAsJsonPrimitive("file_name").getAsString();
                    }
                    return resultJo.getAsJsonPrimitive("path").getAsString();
                } else {
                    throw new Exception("文件服务器返回错误" + statusJo.getAsJsonPrimitive("description").getAsString());
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            logger.error("上传文件失败(" + fileurl + "):" + e);
            throw new Exception("上传文件失败");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

	/**
	 * 生产文件名 时间+8位的随机串
	 * 
	 * @param suffix
	 *            后缀名
	 * @return
	 */
	public String createFileName(String suffix) {
		StringBuilder fileName = new StringBuilder();
		fileName.append(System.currentTimeMillis());
		fileName.append(MsgUtils.generateNoncestr(8));
		fileName.append(ImgTypeEnums.DEFAULT.getCode());
		fileName.append(".").append(suffix);
		return fileName.toString();
	}

	/**
	 * 根据名字的hash
	 * 
	 * @return
	 */
	/**
	 * public String createSavePath() { StringBuilder pathBuf = new
	 * StringBuilder(); pathBuf.append(fileConfiguration.getBaseSavePath());
	 * Random random = new Random(System.currentTimeMillis());
	 * pathBuf.append(getHexValue(Integer.toHexString(random.nextInt(fileConfiguration.getSaveDirSize()))));
	 * pathBuf.append("/");
	 * pathBuf.append(getHexValue(Integer.toHexString(random.nextInt(fileConfiguration.getSaveDirSize()))));
	 * pathBuf.append("/"); return pathBuf.toString(); }
	 */

	/**
	 * 获取16进制的值
	 * 
	 * @param value
	 * @return
	 */
	private String getHexValue(String value) {
		if (value.length() == 1) {
			StringBuilder hexValue = new StringBuilder();
			hexValue.append("0").append(value);
			return hexValue.toString();
		}
		return value;
	}

}
