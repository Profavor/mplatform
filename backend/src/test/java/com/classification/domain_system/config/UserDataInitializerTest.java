package com.classification.domain_system.config;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private UserDataInitializer userDataInitializer;

    @Test
    @DisplayName("UserDataInitializer 실행 시 설치 마법사가 처리하므로 생성을 스킵한다")
    void run_createsAdminUserWhenNotExists() {
        // when
        userDataInitializer.run(applicationArguments);

        // then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("UserDataInitializer 실행 시 이미 존재하는 유저도 업데이트하지 않고 스킵한다")
    void run_updatesAdminUserWhenAlreadyExists() {
        // when
        userDataInitializer.run(applicationArguments);

        // then
        verify(userRepository, never()).save(any(User.class));
    }
}
