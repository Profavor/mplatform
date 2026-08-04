package com.classification.domain_system.controller;

import com.classification.domain_system.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @Mock
    private com.classification.domain_system.service.SystemSeedDumpService systemSeedDumpService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MenuController menuController;

    @Test
    @DisplayName("X-Forwarded-For 헤더에 여러 IP가 나열된 경우 첫 번째 프록시 IP만 45자 이내로 파싱하여 메뉴 액세스 로그에 저장한다")
    void testLogAccessParsesFirstIpFromMultiProxyHeader() {
        String rawMultiIpHeader = "203.0.113.195, 70.41.3.18, 150.172.238.178, 10.200.0.15";
        when(request.getHeader("X-Forwarded-For")).thenReturn(rawMultiIpHeader);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        Map<String, Object> payload = Map.of("menuId", 1, "menuPath", "/admin/users");

        var response = menuController.logAccess(payload, null, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(menuService).logAccess(
                eq(1L),
                eq("/admin/users"),
                eq("anonymous"),
                eq("Mozilla/5.0 (Windows NT 10.0; Win64; x64)"),
                eq("203.0.113.195")
        );
    }
}
