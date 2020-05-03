package com.xfhl.common.api.service;

/**
 * @author: Jiekai Lin
 * @Description(描述): 转换服务
 * @date: 2020/4/22 14:16
 */
public interface ITranslateService {

    /**
     * @Description(描述): 简体转换繁体
     * @auther: Jack Lin
     * @param :[file, dto]
     * @return :java.lang.String
     * @date: 2020/4/17 19:02
     */
    String simpleToTraditional (String str);


}