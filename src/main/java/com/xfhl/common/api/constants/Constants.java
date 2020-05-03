package com.xfhl.common.api.constants;

public class Constants {

	// 处理结果
	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAIL";
	public static final String REFUND = "REFUND";
	
	public static final String ENCODE_UTF8 = "UTF-8";
	public static final String ENCODE_GBK = "GBK";

	public static final String MIMETYPE = "text/plain";
	
	public static final String RETURNCODE = "returnCode";
	public static final String RETURNMSG = "returnMsg";
	public static final String DATA = "data";
	
	public static final String SUCCESS_CODE = "10000";
	public static final String SUCCESS_MSG = "交易成功";
	
	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULER_SWITCH = 0;
	
	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer MQ_SWITCH = 1;
	
	//缓存存3*24h
	public static int CACHE_EXP_TIME = 60 * 60 * 24 * 3;

	public static int CACHE_TIME = 60 * 5;
	
	//加锁等待时间(秒)
	public static int LOCK_WAIT_TIME = 30;
	
	//加锁过期时间，超时强制解锁时间(秒)
	public static int LOCK_LEASE_TIME = 10;
	
	//订单完成锁KEY
	public final static String ORDER_FINISH_LOCK_KEY = "ORDERFINISH";
	//订单完成锁KEY
	public final static String ORDER_PROFIT_LOCK_KEY = "ORDER_PROFIT";
	//在途收益转账户余额-锁KEY
    public final static String INCOME_CHANGE_DEPOSIT_LOCK_KEY = "INCOMECHANGEDEPOSIT";
	
	//公司账号
	public static final Long COMPANY_USERID = 1L;
	
	public static final String LOGIN_TOKEN_KEY = "X-MPMALL-Token";
	public static final String X_MPMALL_SIGNVER = "X-MPMALL-SignVer";
	public static final String X_MPMALL_SIGN = "X-MPMALL-Sign";
}
