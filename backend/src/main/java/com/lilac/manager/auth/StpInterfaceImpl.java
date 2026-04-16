package com.lilac.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // todo 根据 loginType (admin/user) 返回对应的权限码
        return new ArrayList<>();
    }
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // todo 根据 loginType 返回角色
        return new ArrayList<>();
    }
}