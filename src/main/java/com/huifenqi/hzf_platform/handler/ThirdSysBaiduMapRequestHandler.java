package com.huifenqi.hzf_platform.handler;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.huifenqi.hzf_platform.configuration.BaiduMapSysHouseConfiguration;
import com.huifenqi.hzf_platform.configuration.SearchServiceConfiguration;
import com.huifenqi.hzf_platform.context.Constants;
import com.huifenqi.hzf_platform.context.dto.request.house.HouseSearchDto;
import com.huifenqi.hzf_platform.context.entity.location.CommunityBase;
import com.huifenqi.hzf_platform.context.exception.BaseException;
import com.huifenqi.hzf_platform.context.exception.ErrorMsgCode;
import com.huifenqi.hzf_platform.context.response.BdResponses;
import com.huifenqi.hzf_platform.dao.repository.location.CommunityBaseRepository;
import com.huifenqi.hzf_platform.dao.repository.platform.PlatformCustomerRepository;
import com.huifenqi.hzf_platform.utils.HttpUtil;
import com.huifenqi.hzf_platform.utils.LogUtils;
import com.huifenqi.hzf_platform.utils.ThirdSysUtil;

/**
 * ClassName: HouseRequestHandler date: 2016年4月26日 下午4:40:45 Description:
 *
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
@Component
public class ThirdSysBaiduMapRequestHandler {

	private static final Log logger = LogFactory.getLog(ThirdSysBaiduMapRequestHandler.class);


	@Autowired
	private BaiduMapSysHouseConfiguration baiduMapSysHouseConfiguration;
	
    @Autowired
    private SearchServiceConfiguration searchServiceConfiguration;

	@Autowired
	private CommunityBaseRepository communityBaseRepository;

	@Autowired
	private PlatformCustomerRepository platformCustomerRepository;



	/**
	 * 同步小区数据到百度地图
	 *
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public BdResponses sysCommunityData() {
		try {
		    
		    //获取百度地图已挂接的小区
		    List<CommunityBase> commList = communityBaseRepository.findCommunityByBaiduMapFlag(Constants.CommunityStatus.BAIDU_MAP_YES_FLAG);
		    if(CollectionUtils.isEmpty(commList)){
		        return null;
		    }
		    
		    for(CommunityBase c : commList){
		        //根据小区名称获取房源列表
		        JsonArray jsonArray = searchCommunityHouseList(c);
		        
		        //同步房源数到百度地图
		        String status = baiduMapSetdetail(jsonArray,c);
		        
		        //返回结果
		        if(status.equals("ok")){//同步成功
		            communityBaseRepository.updateCommunityBaseStatus(Constants.CommunityStatus.BAIDU_MAP_SUCCESS_STATUS,c.getId());
		        }else{//同步失败
	                communityBaseRepository.updateCommunityBaseStatus(Constants.CommunityStatus.BAIDU_MAP_FAILE_STATUS,c.getId());
		        }
		    }
		    

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BdResponses bdResponses = new BdResponses();
		bdResponses.setCode(ErrorMsgCode.ERROR_MSG_OK);
		bdResponses.setMsg("小区信息同步成功");
		return bdResponses;
	}

	
    /**
     * 搜索服务发起请求
     */
    public JsonArray searchCommunityHouseList(CommunityBase communityName) {
        HouseSearchDto houseSearchDto = new HouseSearchDto();
        houseSearchDto.setCommunityName(communityName.getCommunityName());
        houseSearchDto.setCityId(communityName.getCityId());
        
        String url = String.format("http://%s/baiduMap/queryHouse/", searchServiceConfiguration.getIp());
         
        Map<String, String> params = createHouseListMap(houseSearchDto);
        String postRet = null;
        try {
            postRet = HttpUtil.post(url, params);
        } catch (Exception e) {
            throw new BaseException(e);
        }

        /*  
         "body":{"baiDuMapHouses":[{"sellId":"HF918364674092","roomId":0,"title":"火车南站·桐梓林壹号·4居","rentType":"整租",
                "area":"139","roomType":"","price":8800,"address":"距1号线桐梓林(地铁站)站690米","houseTag":"近地铁,南向",
                "picUrl":"http://hzf-image.oss-cn-beijing.aliyuncs.com/hr_image/HF918364674092/15239287144893vrqiA5z_defalut.jpg?x-oss-process\u003dimage/resize,h_240",
                "detailUrl":"http://m.dev.hzfapi.com/baidumap/houseDetailOne?sellId\u003dHF918364674092\u0026roomId\u003d0",
                "listUrl":"http://m.dev.hzfapi.com/baidumap/?communityName\u003d桐梓林壹号\u0026cityId\u003d0",
                "communityName":"桐梓林壹号","cityId":0}]}
                */
        
        //获取返回body
        JsonElement element =new JsonParser().parse(postRet); 
        JsonObject obj = element.getAsJsonObject();
        JsonObject body = obj.get("body").getAsJsonObject();
        
        //int size = obj.get("meta").getAsJsonObject().get("totalRows").getAsInt();
        JsonArray houses = body.get("baiDuMapHouses").getAsJsonArray();

        return houses;

    }
    
    
    /**
     * 搜索服务发起请求
     */
    public Map<String, String> createHouseListMap(HouseSearchDto houseSearchDto) {
        
        Map<String, String> params = new HashMap<>();
        params.put("appId", String.valueOf(houseSearchDto.getAppId()));
        params.put("cityId", String.valueOf(houseSearchDto.getCityId()));
        params.put("q", String.valueOf(houseSearchDto.getKeyword()));
        params.put("disId", String.valueOf(houseSearchDto.getDistrictId()));
        params.put("bizId", String.valueOf(houseSearchDto.getBizId()));
        params.put("lineId", String.valueOf(houseSearchDto.getLineId()));
        params.put("stationId", String.valueOf(houseSearchDto.getStationId()));
        params.put("price", String.valueOf(houseSearchDto.getPrice()));
        params.put("orientation", String.valueOf(houseSearchDto.getOrientation()));
        params.put("area", String.valueOf(houseSearchDto.getArea()));
        params.put("location", String.valueOf(houseSearchDto.getLocation()));
        params.put("distance", String.valueOf(houseSearchDto.getDistance()));
        params.put("orderType", String.valueOf(houseSearchDto.getOrderType()));
        params.put("order", String.valueOf(houseSearchDto.getOrder()));
        params.put("pageNum", String.valueOf(houseSearchDto.getPageNum()));
        params.put("pageSize", String.valueOf(houseSearchDto.getPageSize()));
        params.put("entireRent", String.valueOf(houseSearchDto.getEntireRent()));
        params.put("bedroomNums", String.valueOf(houseSearchDto.getcBedRoomNums()));
        params.put("houseTag", String.valueOf(houseSearchDto.getHouseTag()));
        params.put("orientationStr", String.valueOf(houseSearchDto.getOrientationStr()));
        params.put("eBedRoomNums", String.valueOf(houseSearchDto.geteBedRoomNums()));
        params.put("sBedRoomNums", String.valueOf(houseSearchDto.getsBedRoomNums()));
        params.put("cBedRoomNums", String.valueOf(houseSearchDto.getcBedRoomNums()));
        params.put("payType", String.valueOf(houseSearchDto.getPayType()));
        params.put("sellerId", String.valueOf(houseSearchDto.getSellerId()));
        params.put("communityName", String.valueOf(houseSearchDto.getCommunityName()));
        params.put("roomerId", String.valueOf(houseSearchDto.getRoomerId()));
        params.put("companyType", String.valueOf(houseSearchDto.getCompanyType()));
        params.put("isHomePage", String.valueOf(houseSearchDto.getIsHomePage()));
        params.put("userId", String.valueOf(houseSearchDto.getUserId()));
        params.put("isCollect", String.valueOf(houseSearchDto.getIsCollect()));
        params.put("isFoot", String.valueOf(houseSearchDto.getIsFoot()));
        
        //删除空value
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();  
        while(it.hasNext()){  
            Map.Entry<String, String> entry=it.next();  
            String key=entry.getKey();
            if(params.get(key).isEmpty() || "null".equals(params.get(key))){//valua is null 
                it.remove();        //delete  
            }  
        } 
        return params;
        
    }
    
    public String baiduMapSetdetail(JsonArray jsonArray,CommunityBase c){
        //获取请求url
        String url = baiduMapSysHouseConfiguration.getUrl();
        String key = baiduMapSysHouseConfiguration.getKey();
        
        //获取请求参数
        Map<String,String> param= ThirdSysUtil.getCommunityParam(key,jsonArray,c);
        //同步小区数据到
        String resPonse = null;
        String myJsonStatus =null;
        try {
            resPonse = HttpUtil.post(url, param);
            logger.info(LogUtils.getCommLog(String.format("同步小区数据到百度地图返回结果:%s",resPonse)));
            
            JsonElement element =new JsonParser().parse(resPonse); 
            JsonObject obj = element.getAsJsonObject();
            if(obj.get("errno").getAsInt() != 0){
                logger.error(LogUtils.getCommLog(String.format("百度地图同步小区失败,小区Id:%s,错误信息:%s", c.getId(),resPonse)));
                return "failed";
            } 
            JsonObject data = obj.get("data").getAsJsonObject();
            myJsonStatus = data.get("status").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myJsonStatus;
        
    }

}


