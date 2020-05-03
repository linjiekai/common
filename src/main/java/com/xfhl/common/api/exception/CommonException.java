package com.xfhl.common.api.exception;


import com.xfhl.common.api.utils.ApplicationYmlUtil;
import lombok.Data;

@Data
public class CommonException extends RuntimeException{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8341547119730680377L;
	private int code;
    private String msg;

    public CommonException() {
        super("");
    }

    public CommonException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CommonException(int code) {
        super(ApplicationYmlUtil.get(code));
        this.code = code;
        this.msg=ApplicationYmlUtil.get(code);
    }

    public CommonException(int code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}
    
    public CommonException(String code, String msg) {
		super(msg);
		this.code = Integer.valueOf(code).intValue();
		this.msg = msg;
	}

    public CommonException(String code, String msg, Throwable e) {
		super(msg, e);
		this.code = Integer.valueOf(code).intValue();
		this.msg = msg;
	}
}
