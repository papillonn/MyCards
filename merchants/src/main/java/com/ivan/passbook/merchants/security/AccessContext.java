package com.ivan.passbook.merchants.security;

/**
 * <h1>用ThreadLocal 去单独存储每一个线程携带的Token 信息</h1>
 * 用ThreadLocal 可以保存每个线程的少量信息
 * @Author Ivan 20:43
 * @Description TODO
 */
public class AccessContext {
//  token是一个ThreadLocal 对象，可以set一个线程独有的值
    private static final ThreadLocal<String> token = new ThreadLocal<>();

    public static String getToken() {
        return token.get();
    }

    public static void setToken(String tokenStr){
        token.set(tokenStr);
    }

    // 清除单独线程的token（threadLocal）
    public static void clearAccessKey(){
        token.remove();
    }

}
