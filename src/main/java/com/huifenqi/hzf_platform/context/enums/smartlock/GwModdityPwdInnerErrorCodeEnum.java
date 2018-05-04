package com.huifenqi.hzf_platform.context.enums.smartlock;

import com.huifenqi.hzf_platform.context.exception.ErrorMsgCode;

public enum GwModdityPwdInnerErrorCodeEnum {
	
	
	OFFLINE("门锁离线，无法修改", ErrorMsgCode.GWLOCK_ERROR_OFFSET + 1),//3430001
	OLD_PWD_FORMAT_ERROR("原密码格式不正确，请重新输入",ErrorMsgCode.GWLOCK_ERROR_OFFSET + 2),//3430002
	NEW_PWD_FORMAT_ERROR("新密码格式不正确，请重新输入", ErrorMsgCode.GWLOCK_ERROR_OFFSET + 3), //3430003
	NOT_SAME("新密码与原密码过于相似，请重新修改",ErrorMsgCode.GWLOCK_ERROR_OFFSET + 4);//3430004
	
	private String desc;
	private Integer code;
	
	private GwModdityPwdInnerErrorCodeEnum(String desc, Integer code) {
		this.desc = desc;
		this.code = code;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}


	public Integer getCode() {
		return code;
	}


	public void setCode(Integer code) {
		this.code = code;
	}


	public static String getDesc(String code) {
        if(code == null){
            return "";
        }
        for (GwModdityPwdInnerErrorCodeEnum o : GwModdityPwdInnerErrorCodeEnum.values()) {
            if(o.code.equals(code)){
                return o.desc;
            }
        }
        return "";
    }
}
