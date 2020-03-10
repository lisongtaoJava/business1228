package com.atlisongtao.business1228.usermanage.mapper;

import com.atlisongtao.business1228.bean.UserInfo;
import tk.mybatis.mapper.common.Mapper;

//interface层只处理业务代码，具体逻辑交给m通用mapper处理
public interface UserInfoMapper extends Mapper<UserInfo>{

}
