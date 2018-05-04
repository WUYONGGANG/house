package com.huifenqi.hzf_platform.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huifenqi.hzf_platform.context.exception.ErrorMsgCode;
import com.huifenqi.hzf_platform.context.response.BdResponses;
import com.huifenqi.hzf_platform.handler.ThirdSysBaiduMapRequestHandler;


@RestController
public class ThirdSysBaiduMapCronller {
	
	@Autowired
	private ThirdSysBaiduMapRequestHandler thirdSysBaiduMapRequestHandler;
	
	
	/**
	 * 同步小区数据到百度地图
	 */
	@RequestMapping(value = "/thirdSys/baidumap/sysCommunityData", method = RequestMethod.POST)
	public BdResponses sysCommunityData(HttpServletRequest request) throws Exception {
		BdResponses bdResponses = null;
		try{
			bdResponses = thirdSysBaiduMapRequestHandler.sysCommunityData();
		}catch(Exception e){
			return new BdResponses(ErrorMsgCode.ERROR_MSG_ADD_PROG_EXCEPION, "同步小区数据到百度地图平台失败");		
		}
		return bdResponses;
	}
	
	
}
