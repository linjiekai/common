package com.xfhl.common.api.enums;

public enum ReqResEnum {

	MSG("msg"),
	X_MP_SIGN("X-MP-Sign"),
    X_MP_SIGN_VER("X-MP-SignVer"),
    X_MP_APPVER("X-MP-APPVer"),
    X_MPMALL_APPVER("X-MPMALL-APPVer"),
    X_MPMALL_SIGN("X-MPMALL-Sign"),
    X_MPMALL_SIGN_VER("X-MPMALL-SignVer"),
    X_MP_SIGN_VER_V1("v1"),
    PLATFORM_ZBMALL("ZBMALL"),
    ORDER_STATUS("orderStatus"),
    LOCAL_IP("127.0.0.1"),
    METHOD_WITHDR_APPLY("WithdrApply"),
    METHOD_WITHDR_AUDIT("WithdrAudit"),
    METHOD_UN_CARD_BIND("UnCardBind"),
    METHOD_CARD_BIND("CardBind"),
    METHOD_CARD_BIND_SMS("CardBindSms"),
    METHOD_CARD_BIND_CONFIRM("CardBindConfirm"),
    METHOD_QUERY_CARD_BIND("QueryCardBind"),
    METHOD_QUERY_WITHDR_BANK("QueryWithdrBank"),
    METHOD_DIRECT_PREPAY("DirectPrePay"),
    METHOD_QUERY_ORDER("QueryOrder"),
    METHOD_QUERY_BALANCE("QueryBalance"),
    METHOD_QUERY_BALANCE_BATCH("QueryBalanceBatch"),
    METHOD_DEPOSIT_RECHARGE("DepositRecharge"),
    METHOD_QUERY_WITHDR_ORDER("QueryWithdrOrder"),
    METHOD_USER_REAL_NAME("UserRealName"),
    METHOD_USER_REAL_NAME_DETAILS_LIST("UserRealNameDetailsList"),
    METHOD_WITHDR_APPLY_ERROR_CHECK("WithdrApplyErrorCheck"),
    REQUEST_ID("requestId"),
    PLATFORM("platform"),
    MERC_ID("mercId"),
    METHOD_TYPE("methodType"),
    TIMESTAMP("timestamp"),
    C_10000("10000"),
    DATA("data"),
    CODE("code"),
    METHOD_ORDERREFUND("OrderRefund"),
    METHOD_QUERYORDERREFUND("QueryOrderRefund"),
    X_MERCID("X-MercId"),
    X_PLATFORM("X-Platform");
    ;

    private Object value;

    ReqResEnum(Object value){
        this.value = value;
    }

    public Integer Integer(){
        return Integer.valueOf(value.toString());
    }

    public String String(){
        return value.toString();
    }
}