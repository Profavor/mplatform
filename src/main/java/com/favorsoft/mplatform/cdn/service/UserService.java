package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.service.external.CommonService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends CommonService<User> {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
