package com.springboot.lolcommunity.user.service;

import com.springboot.lolcommunity.user.dto.UserDto;
import com.springboot.lolcommunity.user.entity.User;

import java.util.Optional;

public interface UserService {
    UserDto.SignResultDto signUp(String email, String password, String name);
    UserDto.SignInResultDto signIn(String email, String password) throws RuntimeException;
    boolean findPw(String email) throws Exception;
    boolean emailDuplicateCheck(String email);
    boolean nicknameDuplicateCheck(String nickname);
    UserDto.PasswordCheckResultDto passwordCheck(String token, String password);
    UserDto.UpdateUserResultDto updateUser(String email, String nickname, String password);
    User getUser();
}