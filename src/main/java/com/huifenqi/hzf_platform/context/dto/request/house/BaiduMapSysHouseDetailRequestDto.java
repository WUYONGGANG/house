package com.huifenqi.hzf_platform.context.dto.request.house;

import com.google.gson.JsonArray;

public class BaiduMapSysHouseDetailRequestDto {
    private String id;
    private int  status;
    private JsonArray data;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public JsonArray getData() {
        return data;
    }
    public void setData(JsonArray data) {
        this.data = data;
    }
   
}
