package dy.whatsong.global.config;


import dy.whatsong.domain.member.entity.Member;
import dy.whatsong.domain.member.service.TokenService;
import dy.whatsong.global.constant.Properties;
import dy.whatsong.global.filter.jwt.CustomAccessDeniedHandler;
import dy.whatsong.global.filter.jwt.CustomAuthenticationEntryPoint;
import dy.whatsong.global.filter.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//    private final TokenService tokenService;
//    private final CorsFilter corsFilter;
    private final Properties.JwtProperties jwtProperties;

    public static final String FRONT_URL = "http://localhost:3000";

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
//                .addFilter(corsFilter);
                .sessionManagement()    // jwt 토큰을 사용하게 되면 세션을 사용하지 않는다고 서버에 명시적으로 선언해 주어야 합니다.
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

            http.cors().configurationSource(request -> {
                var cors = new CorsConfiguration();
                cors.setAllowedOrigins(List.of("http://localhost:3000"));
                cors.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE", "OPTIONS"));
                cors.setAllowedHeaders(List.of("*"));
                return cors;
            });

            http.authorizeRequests()
                    .antMatchers("/oauth/**",
                                            "/**",
                                            "/test",
                                            "/test/**",
                                            "/user/**",
                                            "/user/*",
                                            "/user/kakao/*",
                                            "/api/v1/healthcheck")
                    .permitAll()
                    .antMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
//                .and()
//                    // 403 예외처리 핸들링 - 토큰에 대한 권한과 요청 권한이 달라짐
//                    .exceptionHandling()
//                    .accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                    // 토큰이 없거나 위 변조된 경우
                    .exceptionHandling()
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint(jwtProperties))
                .and()
                    .addFilterBefore(
                            new JwtAuthenticationFilter(jwtProperties),
                            UsernamePasswordAuthenticationFilter.class
                    );

    }
}

