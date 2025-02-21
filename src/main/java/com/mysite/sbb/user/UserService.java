package com.mysite.sbb.user;

import com.mysite.sbb.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    //PasswordEncoder bCpasswordEncoder = new BCryptPasswordEncoder();

    public UserDTO create(UserDTO userDTO){
        SiteUser siteUser = SiteUser.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                //.apiPassword(bCpasswordEncoder.encode(userDTO.getPassword()))
                .build();
        userRepository.save(siteUser);
        return userDTO;
    }


    /*public QuestionDTO getQuestion(Integer id){
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Question not found"));
        return questionMapper.toDTO(question);

    }*/

    public UserDTO getUser(String username){
        SiteUser siteUser = userRepository.findByusername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        return userMapper.toDTO(siteUser);
    }


}
