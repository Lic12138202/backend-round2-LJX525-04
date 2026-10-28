package com.studio.auth.exception;

/**
 * 业务异常：携带 HTTP 状态码与错误信息，
 * 由 GlobalExceptionHandler 统一转换为响应。
 */
public class BizException extends RuntimeException {

    private final int status;
    private final int code;

    public BizException(int status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** 参数不合法 -> 400 */
    public static BizException badRequest(String message) {
        return new BizException(400, 400, message);
    }

    /** 资源冲突（如用户名已存在）-> 409 */
    public static BizException conflict(String message) {
        return new BizException(409, 409, message);
    }

    /** 认证失败 -> 401 */
    public static BizException unauthorized(String message) {
        return new BizException(401, 401, message);
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }
}
