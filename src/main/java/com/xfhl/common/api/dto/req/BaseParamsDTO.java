package com.xfhl.common.api.dto.req;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
public class BaseParamsDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer page = 1;// 分页
    private Integer limit = 10;// 每页数量,默认10页
    private String mercId ;// 商户号
    private String platform ;// 平台编号
    private Long userId = 0L;// 用户ID
    private String sysCnl;
    private String timestamp;
}
