package com.huifenqi.hzf_platform.context.entity.phone;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "t_black_phone")
public class BlackPhone {
	
	@Id
	@Column(name = "f_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "f_city")
    private String  city;
	
	@Column(name = "f_phone")
	private String  phone;

	@Column(name = "f_black_type")
    private int  blakType;
	
	@Column(name = "f_desc")
    private String  desc;
	
	@Column(name = "f_status")
    private int  status;
	
	@Column(name = "f_create_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date  createTime;
	
	@Column(name = "f_update_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date  updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getBlakType() {
        return blakType;
    }

    public void setBlakType(int blakType) {
        this.blakType = blakType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "BlackPhone [id=" + id + ", city=" + city + ", phone=" + phone + ", blakType=" + blakType + ", desc="
                + desc + ", status=" + status + ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
    }
	
	
}
