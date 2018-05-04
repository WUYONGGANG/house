/** 
 * Project Name: hzf_platform_project 
 * File Name: BaiduConfiguration.java 
 * Package Name: com.huifenqi.hzf_platform.configuration 
 * Date: 2016年5月4日下午2:55:16 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */  
package com.huifenqi.hzf_platform.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 
 * ClassName: BaiduConfiguration
 * date: 2016年5月4日 下午2:55:16
 * Description: 
 * 
 * @author changmingwei 
 * @version  
 * @since JDK 1.8 
 */
@Component
public class BaiduMapSysHouseConfiguration {
	
	@Value("${hzf.baiduamp.sys.setdetail.url}")
	private String url;
	
	@Value("${hzf.baiduamp.sys.setdetail.src}")
	private String src;
	
	@Value("${hzf.baiduamp.sys.setdetail.key}")
    private String key;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
	

	
}
