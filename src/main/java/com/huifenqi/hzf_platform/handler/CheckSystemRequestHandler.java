package com.huifenqi.hzf_platform.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.huifenqi.hzf_platform.context.response.BdResponses;

@Service
public class CheckSystemRequestHandler {
    
    public BdResponses checkStatus(HttpServletRequest request) throws Exception {
        
        BdResponses responses = new BdResponses();
        responses.setCode(200);
        responses.setMsg("success");
        return responses;
    }

}
