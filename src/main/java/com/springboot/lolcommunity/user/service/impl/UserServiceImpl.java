package com.springboot.lolcommunity.user.service.impl;

import com.springboot.lolcommunity.config.security.SecurityUtil;
import com.springboot.lolcommunity.user.dto.UserDto;
import com.springboot.lolcommunity.user.service.EmailService;
import com.springboot.lolcommunity.user.service.UserService;
import com.springboot.lolcommunity.config.security.token.JwtTokenProvider;
import com.springboot.lolcommunity.user.entity.User;
import com.springboot.lolcommunity.user.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Service
public class UserServiceImpl implements UserService {

    public UserRepository userRepository;
    public PasswordEncoder passwordEncoder;
    public EmailService emailService;
    public JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public UserDto.SignResultDto signUp(String email, String password, String name) {
        User user;
        user = User.builder()
                    .email(email)
                    .nickname(name)
                    .password(passwordEncoder.encode(password))
                    .roles(Collections.singletonList("ROLE_USER")) // 회원가입 시 기본 권한
                    .build();
        userRepository.save(user);
        UserDto.SignResultDto result = UserDto.SignResultDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .build();
        return result;
    }

    @Override
    public UserDto.SignInResultDto signIn(String email, String password) throws RuntimeException {
        User user = userRepository.getByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException();
        }
        UserDto.SignInResultDto result = UserDto.SignInResultDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .token(jwtTokenProvider.createToken(String.valueOf(user.getEmail()),user.getRoles()))
                .build();
        return result;
    }

    @Override
    public boolean findPw(String email) throws Exception{
        User user =  userRepository.getByEmail(email);
        Optional<User> updateUser = Optional.ofNullable(user);
        if(updateUser.isPresent()){
            String code = emailService.sendSimpleMessage(email);
            user.setPassword(passwordEncoder.encode(code));
            userRepository.save(user);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean emailDuplicateCheck(String email){
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean nicknameDuplicateCheck(String nickname){
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public UserDto.PasswordCheckResultDto passwordCheck(String token, String password){
        String info = jwtTokenProvider.getUsername(token);
        User user = userRepository.getByEmail(info);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException();
        }
        UserDto.PasswordCheckResultDto resultDto = new UserDto.PasswordCheckResultDto(user.getEmail(),user.getNickname());
        return resultDto;
    }

    @Override
    public UserDto.UpdateUserResultDto updateUser(String email, String nickname, String password){
        User user = userRepository.getByEmail(email);
        if(!password.equals("null") && !password.isBlank()){
            String changePw = passwordEncoder.encode(password);
            user.setPassword(changePw);
        }
        user.setNickname(nickname);
        userRepository.save(user);
        String token = jwtTokenProvider.createToken(String.valueOf(user.getEmail()),user.getRoles());
        UserDto.UpdateUserResultDto result = UserDto.UpdateUserResultDto.builder()
                .nickname(user.getNickname())
                .token(token)
                .build();
        return result;
    }

    @Override
    public User getUser(){
        return userRepository
                .findById(SecurityUtil.getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("로그인 정보가 없습니다."));
    }
}