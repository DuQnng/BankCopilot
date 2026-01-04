package com.idea.service;

import com.idea.dto.ChangePasswordDTO;
import com.idea.pojo.Result;
import com.idea.pojo.User;
import com.idea.pojo.LoginInfo;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {


    LoginInfo login(User user);

    Result changePassword(Integer userId, ChangePasswordDTO dto);
}
