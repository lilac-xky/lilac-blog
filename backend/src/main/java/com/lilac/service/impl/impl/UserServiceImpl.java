package com.lilac.service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.entity.User;
import com.lilac.service.impl.UserService;
import com.lilac.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* 用户服务实现类
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




