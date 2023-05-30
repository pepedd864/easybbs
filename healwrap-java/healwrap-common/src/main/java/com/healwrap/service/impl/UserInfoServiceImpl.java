package com.healwrap.service.impl;

import com.healwrap.config.WebConfig;
import com.healwrap.entity.constants.Constants;
import com.healwrap.entity.dto.SessionWebUserDto;
import com.healwrap.entity.enums.PageSize;
import com.healwrap.entity.enums.UserIntegralChangeTypeEnum;
import com.healwrap.entity.enums.UserIntegralOperTypeEnum;
import com.healwrap.entity.enums.UserStatusEnum;
import com.healwrap.entity.enums.file.FileUploadTypeEnum;
import com.healwrap.entity.enums.message.MessageStatusEnum;
import com.healwrap.entity.enums.message.MessageTypeEnum;
import com.healwrap.entity.po.UserInfo;
import com.healwrap.entity.po.UserIntegralRecord;
import com.healwrap.entity.po.UserMessage;
import com.healwrap.entity.query.SimplePage;
import com.healwrap.entity.query.UserInfoQuery;
import com.healwrap.entity.query.UserIntegralRecordQuery;
import com.healwrap.entity.query.UserMessageQuery;
import com.healwrap.entity.vo.PaginationResultVO;
import com.healwrap.exception.BusinessException;
import com.healwrap.mappers.UserInfoMapper;
import com.healwrap.mappers.UserIntegralRecordMapper;
import com.healwrap.mappers.UserMessageMapper;
import com.healwrap.service.EmailCodeService;
import com.healwrap.service.UserInfoService;
import com.healwrap.utils.IpAddressTools;
import com.healwrap.utils.StringTools;
import com.healwrap.utils.SysCacheUtils;
import com.healwrap.utils.file.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

  private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

  @Resource
  private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
  @Resource
  private EmailCodeService emailCodeService;
  @Resource
  private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;
  @Resource
  private UserIntegralRecordMapper<UserIntegralRecord, UserIntegralRecordQuery> userIntegralRecordMapper;
  @Resource
  private FileUtils fileUtils;
  @Resource
  private WebConfig webConfig;

  /**
   * 根据条件查询列表
   */
  @Override
  public List<UserInfo> findListByParam(UserInfoQuery param) {
    return this.userInfoMapper.selectList(param);
  }

  /**
   * 根据条件查询列表
   */
  @Override
  public Integer findCountByParam(UserInfoQuery param) {
    return this.userInfoMapper.selectCount(param);
  }

  /**
   * 分页查询方法
   */
  @Override
  public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
    int count = this.findCountByParam(param);
    int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

    SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
    param.setSimplePage(page);
    List<UserInfo> list = this.findListByParam(param);
    PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    return result;
  }

  /**
   * 新增
   */
  @Override
  public Integer add(UserInfo bean) {
    return this.userInfoMapper.insert(bean);
  }

  /**
   * 批量新增
   */
  @Override
  public Integer addBatch(List<UserInfo> listBean) {
    if (listBean == null || listBean.isEmpty()) {
      return 0;
    }
    return this.userInfoMapper.insertBatch(listBean);
  }

  /**
   * 批量新增或者修改
   */
  @Override
  public Integer addOrUpdateBatch(List<UserInfo> listBean) {
    if (listBean == null || listBean.isEmpty()) {
      return 0;
    }
    return this.userInfoMapper.insertOrUpdateBatch(listBean);
  }

  /**
   * 根据UserId获取对象
   */
  @Override
  public UserInfo getUserInfoByUserId(String userId) {
    return this.userInfoMapper.selectByUserId(userId);
  }

  /**
   * 根据UserId修改
   */
  @Override
  public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
    return this.userInfoMapper.updateByUserId(bean, userId);
  }

  /**
   * 根据UserId删除
   */
  @Override
  public Integer deleteUserInfoByUserId(String userId) {
    return this.userInfoMapper.deleteByUserId(userId);
  }

  /**
   * 根据Email获取对象
   */
  @Override
  public UserInfo getUserInfoByEmail(String email) {
    return this.userInfoMapper.selectByEmail(email);
  }

  /**
   * 根据Email修改
   */
  @Override
  public Integer updateUserInfoByEmail(UserInfo bean, String email) {
    return this.userInfoMapper.updateByEmail(bean, email);
  }

  /**
   * 根据Email删除
   */
  @Override
  public Integer deleteUserInfoByEmail(String email) {
    return this.userInfoMapper.deleteByEmail(email);
  }

  /**
   * 根据NickName获取对象
   */
  @Override
  public UserInfo getUserInfoByNickName(String nickName) {
    return this.userInfoMapper.selectByNickName(nickName);
  }

  /**
   * 根据NickName修改
   */
  @Override
  public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
    return this.userInfoMapper.updateByNickName(bean, nickName);
  }

  /**
   * 根据NickName删除
   */
  @Override
  public Integer deleteUserInfoByNickName(String nickName) {
    return this.userInfoMapper.deleteByNickName(nickName);
  }

  /**
   * 注册
   *
   * @param email     邮箱
   * @param emailCode 邮箱验证码
   * @param nickName  昵称
   * @param password  密码
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void register(String email, String emailCode, String nickName, String password) {
    UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
    if (userInfo != null) {
      throw new BusinessException("邮箱已经被注册");
    }
    userInfo = this.userInfoMapper.selectByNickName(nickName);
    if (userInfo != null) {
      throw new BusinessException("昵称已存在");
    }
    emailCodeService.checkEmailCode(email, emailCode);
    // 创建用户
    String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
    UserInfo insertInfo = new UserInfo();
    insertInfo.setUserId(userId);
    insertInfo.setEmail(email);
    insertInfo.setNickName(nickName);
    insertInfo.setPassword(StringTools.encodeMd5(password));
    insertInfo.setJoinTime(new Date());
    insertInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
    insertInfo.setTotalIntegral(0);
    insertInfo.setCurrentIntegral(0);
    this.userInfoMapper.insert(insertInfo);

    // 更新用户积分
    updateUserIntegral(userId, UserIntegralOperTypeEnum.REGISTER, UserIntegralChangeTypeEnum.ADD.getChangeType(), Constants.INTEGRAL_5);
    // 记录消息
    UserMessage userMessage = new UserMessage();
    userMessage.setReceivedUserId(userId);
    userMessage.setMessageType(MessageTypeEnum.SYS.getType());
    userMessage.setCreateTime(new Date());
    userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
    userMessage.setMessageContent(SysCacheUtils.getSysSetting().getRegisterSetting().getRegisterWelcome());
    userMessageMapper.insert(userMessage);
  }

  /**
   * 更新用户积分
   *
   * @param userId       用户id
   * @param operTypeEnum 操作类型
   * @param changeType   变化类型
   * @param integral     变化积分
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateUserIntegral(String userId, UserIntegralOperTypeEnum operTypeEnum, Integer changeType, Integer integral) {
    integral = changeType * integral;
    if (integral == 0) {
      return;
    }
    UserInfo userInfo = this.userInfoMapper.selectByUserId(userId);
    if (UserIntegralChangeTypeEnum.REDUCE.getChangeType().equals(changeType) && userInfo.getCurrentIntegral() + integral < 0) {
      integral = changeType * userInfo.getCurrentIntegral();
    }
    UserIntegralRecord record = new UserIntegralRecord();
    record.setUserId(userId);
    record.setOperType(operTypeEnum.getOperType());
    record.setCreateTime(new Date());
    record.setIntegral(integral);
    this.userIntegralRecordMapper.insert(record);
    // 更新用户积分,不使用先查再更新的方式
    Integer count = this.userInfoMapper.updateIntegral(userId, integral);
    if (count == 0) {
      throw new BusinessException("更新用户积分失败");
    }
  }

  /**
   * 登录
   *
   * @param email    邮箱
   * @param password 密码
   * @param ip       ip地址
   */

  @Override
  public SessionWebUserDto login(String email, String password, String ip) {
    UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
    if (null == userInfo || !userInfo.getPassword().equals(password)) {
      throw new BusinessException("用户名或密码错误");
    }
    if (!UserStatusEnum.ENABLE.getStatus().equals(userInfo.getStatus())) {
      throw new BusinessException("用户已被禁用");
    }
    String ipAddress = IpAddressTools.getIpAddress(ip);
    UserInfo tmpInfo = new UserInfo();
    tmpInfo.setLastLoginTime(new Date());
    tmpInfo.setLastLoginIp(ip);
    tmpInfo.setLastLoginIpAddress(ipAddress);
    this.userInfoMapper.updateByUserId(tmpInfo, userInfo.getUserId());

    SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
    sessionWebUserDto.setNickName(userInfo.getNickName());
    sessionWebUserDto.setProvince(ipAddress);
    sessionWebUserDto.setUserId(userInfo.getUserId());
    if (!StringUtils.isEmpty(webConfig.getAdminEmails()) && ArrayUtils.contains(webConfig.getAdminEmails().split(","), email)) {
      sessionWebUserDto.setIsAdmin(true);
    } else {
      sessionWebUserDto.setIsAdmin(false);
    }
    return sessionWebUserDto;
  }

  /**
   * 修改密码
   *
   * @param email     邮箱
   * @param password  密码
   * @param emailCode 邮箱验证码
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void resetPwd(String email, String password, String emailCode) {
    UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
    if (null == userInfo) {
      throw new BusinessException("邮箱不存在");
    }
    emailCodeService.checkEmailCode(email, emailCode);
    UserInfo tmpInfo = new UserInfo();
    tmpInfo.setPassword(StringTools.encodeMd5(password));
    this.userInfoMapper.updateByEmail(tmpInfo, email);
  }

  @Override
  public void updateUserInfo(UserInfo userInfo, MultipartFile avatar) {
    userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
    if (avatar != null) {
      fileUtils.uploadFile2Local(avatar, userInfo.getUserId(), FileUploadTypeEnum.AVATAR);
    }
  }
}