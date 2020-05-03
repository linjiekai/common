
package com.xfhl.common.api.dto.req;


import lombok.Builder;
import lombok.Data;

@Data
public class StorageDTO extends BaseParamsDTO {
    private String type;
    private String keyName;
    private int isPrivate;  //是否私有 1-是，0-否
    private String ossPath; //文件路径
    private String fileName; //文件名
    private String stsToken;

}
