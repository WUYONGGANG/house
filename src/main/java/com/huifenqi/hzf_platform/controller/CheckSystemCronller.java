package com.huifenqi.hzf_platform.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huifenqi.hzf_platform.context.response.BdResponses;
import com.huifenqi.hzf_platform.handler.CheckSystemRequestHandler;

@RestController
public class CheckSystemCronller {
    
    
    @Autowired
    private CheckSystemRequestHandler checkSystemRequestHandler;
    /**
     * 监测系统运行是否正常
     */
    @RequestMapping(value = "/common/checkstatus", method = RequestMethod.POST)
    public BdResponses checkStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return checkSystemRequestHandler.checkStatus(request);
    }
}
