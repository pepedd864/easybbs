package com.healwrap.entity.po;

import java.util.Date;
import com.healwrap.entity.enums.DateTimePatternEnum;
import com.healwrap.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 *
 * 用户信息
 *
 */
@Data
public class UserInfo implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 0:女 1:男
	 */
	private Integer sex;

	/**
	 * 个人描述
	 */
	private String personDescription;

	/**
	 * 加入时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date joinTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 最后登录IP
	 */
	private String lastLoginIp;

	/**
	 * 最后登录ip地址
	 */
	private String lastLoginIpAddress;

	/**
	 * 积分
	 */
	private Integer totalIntegral;

	/**
	 * 当前积分
	 */
	private Integer currentIntegral;

	/**
	 * 0:禁用 1:正常
	 */
	private Integer status;

	@Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，昵称:"+(nickName == null ? "空" : nickName)+"，邮箱:"+(email == null ? "空" : email)+"，密码:"+(password == null ? "空" : password)+"，0:女 1:男:"+(sex == null ? "空" : sex)+"，个人描述:"+(personDescription == null ? "空" : personDescription)+"，加入时间:"+(joinTime == null ? "空" : DateUtils.format(joinTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登录时间:"+(lastLoginTime == null ? "空" : DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登录IP:"+(lastLoginIp == null ? "空" : lastLoginIp)+"，最后登录ip地址:"+(lastLoginIpAddress == null ? "空" : lastLoginIpAddress)+"，积分:"+(totalIntegral == null ? "空" : totalIntegral)+"，当前积分:"+(currentIntegral == null ? "空" : currentIntegral)+"，0:禁用 1:正常:"+(status == null ? "空" : status);
	}
}