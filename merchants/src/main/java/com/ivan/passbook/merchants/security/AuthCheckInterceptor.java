package com.ivan.passbook.merchants.security;

import com.ivan.passbook.merchants.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>权限拦截器</h1>
 * @Author Ivan 20:47
 * @Description TODO
 */
//拦截器是一个springbean，需要注册到容器中，所以有了Component注解
@Component
public class AuthCheckInterceptor implements HandlerInterceptor {
    /**
     * 根据token对http请求进行拦截
     * 防止非法请求
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) throws Exception {
        String token = httpServletRequest.getHeader(Constants.TOKEN_STRING);

        if (StringUtils.isEmpty(token)){
            throw new Exception("Header 中缺少 "+Constants.TOKEN_STRING+"!");
        }
        if (!token.equals(Constants.TOKEN)){
            throw new Exception("Header 中 "+Constants.TOKEN_STRING+"错误！");
        }
        //AccessContext里面有一个threadLocal，把这个token设置到这个threadlocal中，方便我们之后在代码中使用这个token
        AccessContext.setToken(token);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //一般这个函数里面不做处理
    }

    /**
     * HTTP请求完成之后做的操作，即使发生异常也会执行
     * @param httpServletRequest
     * @param httpServletResponse
     * @param o
     * @param e
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //清除token信息
        AccessContext.clearAccessKey();

    }
}
