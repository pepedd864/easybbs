package com.easybbs.aspect;

import com.easybbs.entity.annotation.GlobalIntercepter;
import com.easybbs.entity.annotation.VerifyParams;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.exception.BusinessException;
import com.easybbs.utils.StringTools;
import com.easybbs.utils.VerifyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * @ClassName OperationAspect
 * @Description TODO
 * @Date 2023/4/13 10:03
 * @author pepedd
 */

@Component
@Aspect
public class OperationAspect {
  private static final Logger logger = LoggerFactory.getLogger(OperationAspect.class);

  private static final String[] TYPE_BASE = {"java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float", "java.lang.Boolean", "java.lang.Short", "java.lang.Byte", "java.lang.Character", "java.lang.Number", "java.lang.Object", "java.util.Date", "java.sql.Date", "java.sql.Timestamp", "java.math.BigDecimal", "java.math.BigInteger"};

  /**
   * 校验参数
   */
  @Pointcut("@annotation(com.easybbs.entity.annotation.GlobalIntercepter)")
  private void requestIntercepter() {
  }

  /**
   * 校验参数
   *
   * @param pjp 切点
   * @return Object对象
   */
  @Around("requestIntercepter()")
  public Object requestIntercepterDo(ProceedingJoinPoint pjp) {
    try {
      Object target = pjp.getTarget();
      Object[] args = pjp.getArgs();
      String methodName = pjp.getSignature().getName();
      Class<?>[] parameterTypes = ((MethodSignature) pjp.getSignature()).getMethod().getParameterTypes();
      Method method = target.getClass().getMethod(methodName, parameterTypes);
      GlobalIntercepter intercepter = method.getAnnotation(GlobalIntercepter.class);
      if (null == intercepter) {
        return null;
      }
      // 校验登录
      if (intercepter.checkLogin()) {
        checkLogin();
      }
      // 校验参数
      if (intercepter.checkParams()) {
        validateParams(method, args);
      }
      Object result = pjp.proceed();
      return result;
    } catch (BusinessException e) {
      logger.error("全局拦截器异常", e);
      throw e;
    } catch (Exception e) {
      logger.error("全局拦截器异常", e);
      throw new BusinessException(ResponseCodeEnum.CODE_500);
    } catch (Throwable e) {
      logger.error("全局拦截器异常", e);
      throw new BusinessException(ResponseCodeEnum.CODE_500);
    }
  }

  /**
   * 校验参数
   *
   * @param method 方法
   * @param args   参数
   */
  private void validateParams(Method method, Object[] args) {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      Object value = args[i];

      VerifyParams verifyParams = parameter.getAnnotation(VerifyParams.class);
      if (null == verifyParams) {
        continue;
      }
      if (ArrayUtils.contains(TYPE_BASE, parameter.getType().getName())) {
        checkValue(value, verifyParams);
      } else {
        // TODO
      }
    }
  }

  /**
   * 校验参数
   *
   * @param value        参数值
   * @param verifyParams 注解
   */
  private void checkValue(Object value, VerifyParams verifyParams) {
    Boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
    Integer length = value == null ? 0 : value.toString().length();
    /**
     * 校验是否为空
     */
    if (isEmpty && verifyParams.required()) {
      throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    /**
     * 校验长度
     */
    if (verifyParams.max() != -1 && length > verifyParams.max() || verifyParams.min() != -1 && length < verifyParams.min()) {
      throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    /**
     * 校验正则
     */
    if (!StringTools.isEmpty(verifyParams.regex().getRegex()) && !VerifyUtils.verify(verifyParams.regex(), value.toString())) {
      throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
  }

  private void checkObject(Parameter parameter, Object value) {
    // TODO
  }

  private void checkLogin() {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    HttpSession session = request.getSession();
    Object obj = session.getAttribute(Constants.SESSIONS_KEY);
    if (null == obj) {
      throw new BusinessException(ResponseCodeEnum.CODE_901);
    }
  }
}
