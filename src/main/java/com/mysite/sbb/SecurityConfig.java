package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity //모든 요청 URL이 스프링 시큐리티의 제어를 받도록
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    //빈?
    // 빈은 스프링에 의해 생성 또는 관리되는 객체
    // @Bean 애너테이션을 통해 자바 코드 내에서 별도로 빈을 정의 하고 등록할 수 있다
    @Bean //스프링 시큐리티의 세부성정은 @Bean 애너테이션을 통해 SecurityFilterChain 빈을 생성하여 설정
    SecurityFilterChain filterChanin(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/user/support/create-ajax"),
                        new AntPathRequestMatcher("/admin/categories/delete-parents", "POST") // 최소 범위
                ))
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
                //formLogin 메서드는 스프링 시큐리티의 로그인 설정을 담당하는 부분
                //로그인 페이지의 URL은 "/user/login"이고 로그인 성공시 "/"으로 이동한다.
                .formLogin((formLogin) -> formLogin
                        .loginPage("/siteUser/login")
                        .defaultSuccessUrl("/admin/product/list"))
        .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/siteUser/logout"))
                .logoutSuccessUrl("/admin/login")
                .invalidateHttpSession(true));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
        return new Sha1PasswordEncoder(); //password를 SHA-1로 암호화
    }

    //AuthenticationManager 생성
    //AuthenticationManager는 사용자 인증시 앞에서 작성한 UserSecurityService와 PasswordEncoder를 내부적으로 사용하여 인증과 권한부여 프로세스를 처리한다.
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
}
