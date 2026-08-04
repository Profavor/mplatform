package com.classification.domain_system.config;

import com.classification.domain_system.dto.MenuSeedDto;
import com.classification.domain_system.entity.Menu;
import com.classification.domain_system.repository.MenuRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuDataInitializerTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MenuDataInitializer menuDataInitializer;

    @Test
    @DisplayName("메뉴 데이터가 없을 때 default_menus.json을 통해 초기화한다")
    void initMenus_seedsDefaultMenuTreeWhenEmpty() throws Exception {
        // given
        given(menuRepository.count()).willReturn(0L);
        
        MenuSeedDto dto1 = new MenuSeedDto();
        dto1.setId(1L);
        dto1.setName("{\"ko\":\"홈\"}");
        dto1.setPath("/");
        
        MenuSeedDto dto2 = new MenuSeedDto();
        dto2.setId(2L);
        dto2.setName("{\"ko\":\"관리자\"}");
        dto2.setPath("/admin");
        dto2.setParentId(1L);
        
        List<MenuSeedDto> defaultMenus = List.of(dto1, dto2);
        
        // Mocking the ObjectMapper to return our test data when reading the input stream
        given(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                .willReturn(defaultMenus);

        // Mocking the save behavior
        Menu savedMenu1 = new Menu();
        savedMenu1.setId(1L);
        Menu savedMenu2 = new Menu();
        savedMenu2.setId(2L);
        
        given(menuRepository.save(any(Menu.class)))
                .willReturn(savedMenu1)
                .willReturn(savedMenu2);

        // when
        menuDataInitializer.initMenus();

        // then
        verify(menuRepository, times(2)).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 데이터가 이미 존재하면 초기화를 건너뛴다")
    void initMenus_skipsWhenDataExists() {
        // given
        given(menuRepository.count()).willReturn(15L);

        // when
        menuDataInitializer.initMenus();

        // then
        verify(menuRepository, never()).save(any(Menu.class));
    }
}
