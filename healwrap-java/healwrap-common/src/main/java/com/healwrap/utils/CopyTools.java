package com.healwrap.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pepedd
 * @ClassName CopyTools
 * @Description 复制工具类
 * @Date 2023/4/25 0:49
 */
public class CopyTools {
  public static <T, S> List<T> copyList(List<S> sList, Class<T> tClass) {
    List<T> list = new ArrayList<>();
    for (S s : sList) {
      T t = null;
      try {
        t = tClass.newInstance();
      } catch (Exception e) {
        e.printStackTrace();
      }
      BeanUtils.copyProperties(s, t);
      list.add(t);
    }
    return list;
  }

  public static <T, S> T copy(S s, Class<T> tClass) {
    T t = null;
    try {
      t = tClass.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
    BeanUtils.copyProperties(s, t);
    return t;
  }
}
