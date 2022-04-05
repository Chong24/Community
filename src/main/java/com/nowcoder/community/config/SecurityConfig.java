package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * spring security的配置类
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //因为检查用户名、密码、验证码、请记住我的操作我们之前都已经实现了，可以直接用之前的，就没必要自定义这方面的认证准则了，没必要AuthenticationProvider，
    //因此我们只需要进行权限管理即可。
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests().antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                ).hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful",
                        "/discuss/unTop",
                        "/discuss/unwonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll().and().csrf().disable();
        //关闭了自动防止csrf攻击，如果开启就要配置token，并每个异步请求都要配置

        //权限不够时的处理
        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
            //处理没有登陆的情况
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                //我们得知道是希望我们返回什么数据类型的交互信息，从请求头中获取“x-requested-with”字段
                String xRequestWith = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestWith)) {
                    //如果是xml，以前是xml，现在都被JSON替代了，即要返回json对象。一般异步就是返回JSON
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommunityUtil.getJSONString(403, "您还没有登录"));
                } else {
                    //如果不是xml，即要返回HTML，页面
                    response.sendRedirect(request.getContextPath() + "/login");
                }
            }
        }).accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                String xRequestWith = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestWith)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommunityUtil.getJSONString(403, "您还没有访问此功能的权限"));
                } else {
                    response.sendRedirect(request.getContextPath() + "/denied");
                }
            }
        });

        //Security底层默认会拦截/logout请求，让进行退出处理
        //所以要想让它底层的不生效(就让他处理一个与业务逻辑无关的请求，这样当请求路径为logout，底层找不到，就会执行我们的)，
        // 那我们就需要覆盖它默认的逻辑。执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");
    }
}
