package com.xfhl.common.api.dto.req;

import lombok.Data;

@Data
public class SmsDTO {

    String templateId; //短信模板id
    String mobileCode;  //区号
    String mobile;  //手机号码
    String json;    //模版参数
    String platForm; //商户名
}
