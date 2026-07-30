package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testSearchUsers() {
        // Given
        User u1 = new User();
        u1.setId("1");
        u1.setUsername("admin");
        
        User u2 = new User();
        u2.setId("2");
        u2.setUsername("admin2");

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(Arrays.asList(u1, u2), pageable, 2);

        when(userRepository.findByUsernameContainingIgnoreCaseOrRoleContainingIgnoreCase(eq("admin"), eq("admin"), eq(pageable)))
                .thenReturn(mockPage);

        // When
        Page<User> result = userService.searchUsers("admin", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
