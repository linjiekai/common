
package com.xfhl.common.api.config;

import lombok.Data;

@Data
public class Platform {


    private String mercId; //商户号
    private String mercName; //商户名称
    private String mercCode; //商户编码



    //短信的
    private String accessName;//境内签名
    private String accessNameOverSea;//海外签名


    //oss的
    private String bucketName;
    private String url;
    private Long expires;
    private String privateBucketName;
    private String stsDomain;
    private String roleArn;
    private String roleSessionName;
    private String stsAppRoleArn;
    private String stsAppRoleSessionName;

}