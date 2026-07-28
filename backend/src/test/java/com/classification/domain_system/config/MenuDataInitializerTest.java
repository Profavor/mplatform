package com.classification.domain_system.config;

import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuDataInitializerTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuDataInitializer menuDataInitializer;

    @Test
    @DisplayName("메뉴 데이터가 없을 때 시스템 기본 메뉴 트리를 DB에 시딩한다")
    void initMenus_seedsDefaultMenuTreeWhenEmpty() {
        // given
        given(menuRepository.findAll()).willReturn(Collections.emptyList());
        given(menuRepository.save(any(Menu.class))).willAnswer(invocation -> {
            Menu m = invocation.getArgument(0);
            if (m.getId() == null) {
                m.setId(100L);
            }
            return m;
        });

        // when
        menuDataInitializer.initMenus();

        // then
        // Total default menus created: 6 top-level + 1 admin parent + 7 admin sub-menus = 14 menus
        verify(menuRepository, atLeast(7)).save(any(Menu.class));
    }
}
