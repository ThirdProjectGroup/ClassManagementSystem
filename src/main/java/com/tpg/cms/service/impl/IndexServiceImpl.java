package com.tpg.cms.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tpg.cms.model.SysRole;
import com.tpg.cms.model.SysUser;
import com.tpg.cms.service.IndexService;
import com.tpg.cms.service.PermissionService;
import com.tpg.cms.service.SyRoleService;
import com.tpg.cms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SyRoleService syRoleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据用户名获取用户登录信息
     *
     * @param username
     * @return
     */
    @Override
    public Map<String, Object> getUserInfo(String username) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = sysUserService.selectByUsername(username);
        if (null == user) {
            //throw new MyException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }

        //根据用户id获取角色
        List<SysRole> roleList = syRoleService.selectRoleByUserId(user.getId());
        List<String> roleNameList = roleList.stream().map(item -> item.getRoleName()).collect(Collectors.toList());
        if(roleNameList.size() == 0) {
            //前端框架必须返回一个角色，否则报错，如果没有角色，返回一个空角色
            roleNameList.add("");
        }

        //根据用户id获取操作权限值
        List<String> permissionValueList = permissionService.selectPermissionValueByUserId(user.getId());
        redisTemplate.opsForValue().set(username, permissionValueList);

        result.put("name", user.getUsername());
        result.put("avatar", "https://lh3.googleusercontent.com/proxy/z3hSLGDFWf5m3s59ngKjlyOOnYueIFQBLVvz2m98jLSxpGvLBFPZD4XXvmFhGbnndjsMNyyEtmAbRzo6VvCq5Ud5SetM_YC4nntDZGpQdeKbkOCmg6Y");
        result.put("roles", roleNameList);
        result.put("permissionValueList", permissionValueList);
        return result;
    }

    /**
     * 根据用户名获取动态菜单
     * @param username
     * @return
     */
    @Override
    public List<JSONObject> getMenu(String username) {
        SysUser user = sysUserService.selectByUsername(username);

        //根据用户id获取用户菜单权限
        List<JSONObject> permissionList = permissionService.selectPermissionByUserId(user.getId());
        return permissionList;
    }


}
