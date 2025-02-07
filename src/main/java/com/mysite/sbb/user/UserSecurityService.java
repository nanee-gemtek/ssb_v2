package com.mysite.sbb.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


//스프링 시큐리티가 로그인시 사용할 UserSecurityService는 시큐리티가 제공하는 UserDetailService 인터페이스를 구현한다.
//UserDetailService는 loadUserByUsername 메서드를 구현하도록 강제하는 인터페이스
//loadUserByUsername메서드는 사용자명(username)으로 스프링 시큐리티의 사용자(User)객체를 조회하여 리턴
@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    //loadUserByUsername메서드는 사용자명으로 SiteUser 객체를 조회하고, 만약 사용자명에 해당하는 데이터가 없을 경우 UsernameNotFoundException을 발생시킨다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SiteUser> _sireUser = this.userRepository.findByusername(username);

        if(_sireUser.isEmpty()){
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
        SiteUser siteUser = _sireUser.get();
        List<GrantedAuthority> authorities = new ArrayList<>();

        if("admin".equals(username)){
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        }else{
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }

        //스프링시큐리티는 loadUserByUsername 메서드에 의해 리턴된 User객체릐 비밀번호가 사용자로 부터 입력받은 비밀번호와 일치하는지를 검사하는 기능을 내부에 가짐
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
