package com.huifenqi.hzf_platform.vo;

import java.util.List;

public class RentContractBugetVo {
	
    private String rentContractCode;
    private int contractType;
    private String agencyPhone;
    private String agencyName;
    private String address;
    private int contractEffectStatus;
    private int contractConfirm;
    private String contractConfirmName;
    private List<RentBugetDetailVo> bugets;
    
	public String getRentContractCode() {
		return rentContractCode;
	}
	
	public void setRentContractCode(String rentContractCode) {
		this.rentContractCode = rentContractCode;
	}
	
	public String getAgencyPhone() {
		return agencyPhone;
	}
	
	public void setAgencyPhone(String agencyPhone) {
		this.agencyPhone = agencyPhone;
	}
	
	public String getAgencyName() {
		return agencyName;
	}
	
	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getContractEffectStatus() {
		return contractEffectStatus;
	}
	
	public void setContractEffectStatus(int contractEffectStatus) {
		this.contractEffectStatus = contractEffectStatus;
	}
	
	public List<RentBugetDetailVo> getBugets() {
		return bugets;
	}
	
	public void setBugets(List<RentBugetDetailVo> bugets) {
		this.bugets = bugets;
	}
	
	public int getContractConfirm() {
        return contractConfirm;
    }

	public void setContractConfirm(int contractConfirm) {
        this.contractConfirm = contractConfirm;
    }

    public String getContractConfirmName() {
        return contractConfirmName;
    }

    public void setContractConfirmName(String contractConfirmName) {
        this.contractConfirmName = contractConfirmName;
    }

    public int getContractType() {
        return contractType;
    }

    public void setContractType(int contractType) {
        this.contractType = contractType;
    }

    @Override
    public String toString() {
        return "RentContractBugetVo [rentContractCode=" + rentContractCode + ", contractType=" + contractType
                + ", agencyPhone=" + agencyPhone + ", agencyName=" + agencyName + ", address=" + address
                + ", contractEffectStatus=" + contractEffectStatus + ", contractConfirm=" + contractConfirm
                + ", contractConfirmName=" + contractConfirmName + ", bugets=" + bugets + "]";
    }

}
