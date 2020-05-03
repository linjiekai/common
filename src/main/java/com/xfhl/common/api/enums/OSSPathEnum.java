package com.xfhl.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OSS图片枚举类
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum OSSPathEnum {

    /*** 展示分类 */
    A_SHOWCATAGORY(1, "7", "showCatagory/", false),
    /*** 管理后台：首页 */
    A_INDEX(1, "5", "index/", false),
    /*** 管理后台：商品属性 */
    A_GOODS_SPEC(1, "4", "goodsspec/", false),
    /*** 管理后台：商品 */
    A_GOODS(1, "2", "goods/", false),
    /*** 管理后台：广告 */
    A_AD(1, "1", "ad/", false),
    /*** 管理后台：动态 */
    A_DYNAMIC(1, "3", "dynamic/", false),
    /*** 管理后台: 品牌 */
    A_BRAND(1, "8", "brand/", false),
    /*** 管理后台: 虚拟用户头像 */
    A_HEAD_PHOTO(1, "9", "headPhoto/", false),

    /*===================================================================*/

    /*** 手机：反馈 */
    M_FEEDBACK(0, "0", "feedback/", false),
    /*** 手机：身份证 */
    M_IDCARD(0, "1", "idCard/", true),
    /*** 手机：银行卡 */
    M_BANK_CARD(0, "2", "bankCard/", true),
    /*** 手机：协议签名 */
    M_AGREEMENT_SIGN(0, "3", "agreementSign/", false),
    /*** 手机：协议签名 */
    M_LIVE_COVER(0, "4", "liveCover/", false),
    ;

    /**
     * 0:手机, 1:管理后台
     */
    private Integer source;
    /**
     * 类型
     */
    private String type;
    /**
     * 路径
     */
    private String path;
    /**
     * 是否私有/是否加密存储
     */
    private boolean isPrivate;

    /**
     * 根据 source 和 type 获取枚举
     *
     * @param source 0:手机，1：管理后台
     * @param type   类型
     * @return
     */
    public static OSSPathEnum getOneBySourceAndType(Integer source, String type) {
        for (OSSPathEnum value : values()) {
            if (value.type.equals(type) && value.source.equals(source)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据source和type获取路径
     *
     * @param source 0:手机，1：管理后台
     * @param type   类型
     * @return
     */
    public static String getPath(Integer source, String type) {
        for (OSSPathEnum value : values()) {
            if (value.type.equals(type) && value.source.equals(source)) {
                return value.path;
            }
        }
        return null;
    }

    /**
     * 根据 条件 获取枚举
     *
     * @param source 0:手机，1：管理后台
     * @param type   类型
     * @return
     */
    public static OSSPathEnum getOne(Integer source, String type,boolean isPrivate) {
        for (OSSPathEnum value : values()) {
            if (value.type.equals(type) && value.source.equals(source)&&value.isPrivate==isPrivate) {
                return value;
            }
        }
        return null;
    }
}