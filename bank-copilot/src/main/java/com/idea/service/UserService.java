package com.idea.service;

import com.idea.dto.ChangePasswordDTO;
import com.idea.entity.Result;
import com.idea.entity.User;
import com.idea.entity.LoginInfo;

public interface UserService {


    LoginInfo login(User user);

    Result changePassword(Integer userId, ChangePasswordDTO dto);
}
