package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.repository.jpa.UserRepository;
import com.favorsoft.mplatform.cdn.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getList() {
        return userRepository.findAll();
    }

    @Override
    public User save(User entity) {
        return userRepository.saveAndFlush(entity);
    }

    @Override
    public User getObject(Object key) {
        return userRepository.findById((String) key).orElse(new User());
    }

    @Override
    public void delete(Object key) {
        userRepository.delete(getObject(key));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getObject(username);
    }
}
