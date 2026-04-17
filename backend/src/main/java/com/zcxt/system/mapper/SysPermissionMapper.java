package com.zcxt.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcxt.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    @Select("""
            select p.permission_key
            from sys_permission p
            join sys_role_permission rp on rp.permission_id = p.permission_id
            where rp.role_id = #{roleId}
            """)
    List<String> selectPermissionKeysByRoleId(String roleId);
}

