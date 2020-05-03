package com.xfhl.common.api.aop;

import com.xfhl.common.api.annotation.LoginUser;
import com.xfhl.common.api.constants.Constants;
import com.xfhl.common.api.enums.PlatformType;
import com.xfhl.common.api.enums.ReqResEnum;
import com.xfhl.common.api.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@Component
@Slf4j
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {



    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Long.class) && parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) {

        String token = request.getHeader(Constants.LOGIN_TOKEN_KEY);
        String platform = request.getHeader(ReqResEnum.X_PLATFORM.String());
        String key=getKey(platform);

        log.info("platform：{}，token:{}",platform, token);
        if (token == null || StringUtils.isBlank(token)) {
    		log.info("token对应的用户是空的1");
            return null;
        }

        Object obj = RedisUtil.get(key+token);
        if (obj == null) {
            log.info("token对应的用户是空的2");
            return null;
        }
        log.info("token ---> id:{}", obj);

        Long userId = Long.valueOf(String.valueOf(obj)).longValue();
        if (Constants.COMPANY_USERID.longValue() == userId) {
        	log.error("公司账号，不允许登录");
        	return null;
        }

        return userId;
    }


    //前缀不同
    public String getKey(String platform){
        String key="";
        switch (PlatformType.parasByCode(platform)){
            case MPMALL: key="xfhl-mpmall-";break;
            case MPWJMALL: key="xfhl-mpmall-";break;
            case XFYLMALL: key="xfhl-xfyinli-";break;
            case ZBMALL: key="xfhl-zhuanbo-";break;
        }
        return key;
    }
}
