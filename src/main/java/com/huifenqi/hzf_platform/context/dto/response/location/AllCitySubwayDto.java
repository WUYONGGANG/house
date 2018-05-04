package com.huifenqi.hzf_platform.context.dto.response.location;

import java.util.ArrayList;
import java.util.List;

public class AllCitySubwayDto {
	
	
	private long version;
	private int updateFlag;
	
	private List<CitySubwayDto> citysSubway = new ArrayList<>();
	
	

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}


	public int getUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(int updateFlag) {
        this.updateFlag = updateFlag;
    }

    public List<CitySubwayDto> getCitysSubway() {
		return citysSubway;
	}

	public void addCitysSubway(CitySubwayDto citySubway) {
		citysSubway.add(citySubway);
	}
	
	
	
}
