package io.github.forezp.modules.system.web;


import com.baomidou.mybatisplus.mapper.Condition;
import io.github.forezp.common.dto.PageResultsDTO;
import io.github.forezp.common.dto.RespDTO;
import io.github.forezp.common.exception.AriesException;
import io.github.forezp.common.util.*;
import io.github.forezp.modules.system.entity.SysMenu;
import io.github.forezp.modules.system.entity.SysUser;
import io.github.forezp.modules.system.service.SysMenuService;
import io.github.forezp.modules.system.service.SysUserService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.forezp.common.exception.ErrorCode.PWD_ERROR;
import static io.github.forezp.common.exception.ErrorCode.TOKEN_ISNULL;
import static io.github.forezp.common.exception.ErrorCode.USER_NOT_EXIST;


/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author forezp
 * @since 2018-08-03
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    LogUtils LOG = new LogUtils(SysUserController.class);

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysMenuService sysMenuService;

    @GetMapping("/pagelist")
    public RespDTO searchUsers(@RequestParam int page, @RequestParam int pageSize, @RequestParam(required = false) String userId, @RequestParam (required = false)String realname) {
        PageUtils.check(page, pageSize);
        PageResultsDTO sysUsers = sysUserService.searchUsers(page, pageSize, userId, realname);

        return RespDTO.onSuc(sysUsers);
    }

    @PostMapping("/login")
    public RespDTO login(@RequestParam String username, @RequestParam String password) {

        LOG.info("login parmas: {},{}", username, password);
        SysUser user = sysUserService.selectOne(Condition.create().eq("user_id", username));
        if (user == null) {
            throw new AriesException(USER_NOT_EXIST);
        }
        if (!user.getPassword().equals(MD5Utils.encrypt(password))) {
            throw new AriesException(PWD_ERROR);
        }
        //登录成功
        String jwt;
        Map<String, String> result = new HashMap<>(1);

        try {
            jwt = JWTUtils.createJWT(user.getId() + "", user.getUserId(), 599999999L);


            result.put("token", jwt);
            LOG.info("login success,{}", jwt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RespDTO.onSuc(result);
    }


    @GetMapping("/info")
    public RespDTO info() {

//        String token=null;
//        try {
//             token= HttpUtils.getHeaders(HttpUtils.getHttpServletRequest()).get("Authorization");
//
//        }catch (Exception e){
//          ExceptionUtils.printRootCauseStackTrace(e);
//        }
//
//        if(StringUtils.isEmpty(token)){
//            throw new AriesException(TOKEN_ISNULL);
//        }
//        try {
//            Claims claims=JWTUtils.parseJWT(token);
//            if(claims!=null){
//                String id=claims.getId();
//                String userId=claims.getSubject();
//
//            }
//        } catch (Exception e) {
//            ExceptionUtils.printRootCauseStackTrace(e);
//        }

        Map<String, Object> result = new HashMap<>();
        List<SysMenu> menus = sysMenuService.selectList(Condition.create().eq("ismenu", 1));

        LOG.info("menuList size:" + menus.size());
        result.put("menus", menus);
        result.put("roles", "administrator");

        return RespDTO.onSuc(result);
    }

}