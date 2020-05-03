package com.xfhl.common.api.interceptor;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.xfhl.common.api.annotation.UnAuthAnnotation;
import com.xfhl.common.api.config.SignKeyConfig;
import com.xfhl.common.api.enums.ReqResEnum;
import com.xfhl.common.api.exception.CommonException;
import com.xfhl.common.api.utils.ApplicationYmlUtil;
import com.xfhl.common.api.utils.ResponseUtil;
import com.xfhl.common.api.utils.Sign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class AuthHandlerInterceptor implements HandlerInterceptor {

    private final String OPTIONAL = "\\[\\w+\\]$";
    private final String MOBILE = "mobile";// 手机参数
    private static final String LOGGER_ID = "LOGGER_ID";

    @Autowired
    private SignKeyConfig signKeyConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        AuthHttpServletRequestWrapper requestWrapper = new AuthHttpServletRequestWrapper(request);
        MDC.put(LOGGER_ID, UUID.randomUUID().toString());
        try {
            //有这个标签代表不用鉴权
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            UnAuthAnnotation methodAnnotation = handlerMethod.getMethodAnnotation(UnAuthAnnotation.class);
            if (methodAnnotation != null) {
                return true;
            }

            // 鉴权
            boolean result = checkAuth(requestWrapper, handler);
            if (!result) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.print(JSON.toJSONString(ResponseUtil.result(99999)));
                writer.close();
                writer.flush();
                return false;
            }
            return true;
        } catch (CommonException e) {
            try {
                log.error("请求地址" + request.getRequestURI() + ",请求参数" + JSON.parseObject(getRequestBody(requestWrapper), HashMap.class), e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw e;
        } catch (Exception e) {
            try {
                log.error("请求地址" + request.getRequestURI() + ",请求参数" + JSON.parseObject(getRequestBody(requestWrapper), HashMap.class), e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            log.error("1、鉴权异常：{}", e);
            return false;
        }
    }

    /**
     * 获取请求参数和sign
     *
     * @param requestWrapper
     * @return map: 有序且去掉sign, sign:签名
     */
    private boolean checkAuth(AuthHttpServletRequestWrapper requestWrapper, Object handler) throws Exception {

        Map<String, Object> params;

        if ("GET".equalsIgnoreCase(requestWrapper.getMethod())) {
            params = getParams(requestWrapper);
        } else if ("POST".equalsIgnoreCase(requestWrapper.getMethod())) {

            String requestBody = getRequestBody(requestWrapper);
            if (StringUtils.isBlank(requestBody)) {// form表单提交
                params = getParams(requestWrapper);
            } else {// body提交
                params = JSON.parseObject(requestBody, HashMap.class);
            }
        } else {
            log.error("不支持的请求类型:{}", requestWrapper.getMethod());
            return false;
        }


        // 校验参数是否合格
        regularExpression(params, handler);

        String singVerKey = ReqResEnum.X_MPMALL_SIGN_VER.String();
        String signVer = requestWrapper.getHeader(ReqResEnum.X_MPMALL_SIGN_VER.String());

        if (StringUtils.isBlank(signVer)) {
            singVerKey = ReqResEnum.X_MP_SIGN_VER.String();
            signVer = requestWrapper.getHeader(ReqResEnum.X_MP_SIGN_VER.String());
        }

        // 签名字符串
        String sign = requestWrapper.getHeader(ReqResEnum.X_MPMALL_SIGN.String());

        if (StringUtils.isBlank(sign)) {
            sign = requestWrapper.getHeader(ReqResEnum.X_MP_SIGN.String());
        }

        String mercId = requestWrapper.getHeader(ReqResEnum.X_MERCID.String());
        String platform = requestWrapper.getHeader(ReqResEnum.X_PLATFORM.String());
        params.put("mercId",mercId);
        params.put("platform",platform);
        boolean checkSing = checkSign(params, sign, signVer);
        if (!checkSing) {
            params.put(singVerKey, requestWrapper.getHeader(singVerKey));
            checkSing = checkSign(params, sign, signVer);
        }
        // 签名校验
        return checkSing;
    }

    private String getRequestBody(AuthHttpServletRequestWrapper requestWrapper) throws Exception {
        ServletInputStream inputStream = requestWrapper.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String str = new String(result.toByteArray(), "UTF-8");
        result.close();
        inputStream.close();
        return str;
    }

    /**
     * 鉴权
     *
     * @param params
     * @param sign
     * @param v
     * @return
     */
    private boolean checkSign(Map<String, Object> params, String sign, String v) {
        String s = JSONUtil.toJsonStr(params);
        if (sign == null || params == null || v == null) {
            log.error("空值sign:{}, params:{}, v:{}", sign, s, v);
            return false;
        }
        log.info("sign:{}, params:{}, v:{}", sign, s, v);
        // 鉴权
        try {
            String mercId = (String) params.get("mercId");
            String signSecret = signKeyConfig.getMap().get(v).get(mercId);// 签名
            String plain = Sign.getPlain(params) + "&key=" + signSecret;
            String signServer = Sign.sign(plain);
            if (!sign.equals(signServer)) {
                log.error("plain:{}", plain);
                log.error("鉴权失败:Get Sign:{}, Server Sign:{}", sign, signServer);
                return false;
            }
        } catch (Exception e) {
            log.error("1、鉴权异常:{}", e);
            return false;
        }
        return true;
    }


    /**
     * 获取请求参数key-value
     *
     * @param requestWrapper
     * @return
     */
    private Map<String, Object> getParams(AuthHttpServletRequestWrapper requestWrapper) {

        HashMap<String, Object> params = new HashMap<>();
        Enumeration<String> parameterNames = requestWrapper.getParameterNames();
        String key;
        while (parameterNames.hasMoreElements()) {
            key = parameterNames.nextElement();
            params.put(key, requestWrapper.getParameter(key));
        }
        return params;
    }

    /**
     * 通过配置文件进行参数校验
     *
     * @param requestParams 请求失败
     * @param handler
     */
    private void regularExpression(Map<String, Object> requestParams, Object handler) {
        String s = JSONUtil.toJsonStr(requestParams);
        log.info("请求参数[{}]", s);
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        String ymlMethodsKey = controllerName + "." + methodName;

        String ymlMethodsParams = ApplicationYmlUtil.get(ymlMethodsKey);
        if (StringUtils.isBlank(ymlMethodsParams)) {
            return;
        }

        String[] ymlMethodsParamsArray = ymlMethodsParams.split(",");
        for (String ymlMethodsParam : ymlMethodsParamsArray) {
            if (ymlMethodsParam.matches(OPTIONAL)) {// 非必传
                if (requestParams == null) {
                    continue;
                }
                Object requestParamValue = requestParams.get(ymlMethodsParam);
                // 传非空，判断
                if (requestParamValue != null && StringUtils.isNotBlank(requestParamValue.toString())) {
                    String reg = ApplicationYmlUtil.get(ymlMethodsKey + "." + ymlMethodsParam);
                    if (StringUtils.isBlank(reg)) {
                        continue;
                    }
                    if (!requestParamValue.toString().matches(reg)) {
                        if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                            throw new CommonException("请输入正确的手机号");
                        }
                        throw new CommonException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                    }
                }
            } else {// 必传
                if (requestParams == null || requestParams.size() == 0) {
                    throw new CommonException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                }
                Object requestParamValue = requestParams.get(ymlMethodsParam);
                // 判断不为空后再正则判断
                if (requestParamValue == null || StringUtils.isBlank(requestParamValue.toString())) {
                    if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                        throw new CommonException("请输入正确的手机号");
                    }
                    throw new CommonException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                } else {
                    String reg = ApplicationYmlUtil.get(ymlMethodsKey + "." + ymlMethodsParam);
                    if (StringUtils.isBlank(reg)) {
                        continue;
                    }
                    if (!requestParamValue.toString().matches(reg)) {
                        if (MOBILE.equalsIgnoreCase(ymlMethodsParam)) {
                            throw new CommonException("请输入正确的手机号");
                        }
                        throw new CommonException("参数<" + ymlMethodsParam + ">请求参数值不能为空或不合法或长度不对");
                    }
                }
            }
        }
    }
}
