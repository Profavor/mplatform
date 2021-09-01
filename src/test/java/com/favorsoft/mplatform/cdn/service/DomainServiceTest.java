package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.Domain;
import com.favorsoft.mplatform.cdn.domain.Message;
import com.favorsoft.mplatform.cdn.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class DomainServiceTest {

    @Autowired
    private DomainService domainService;

    private static Logger logger = LoggerFactory.getLogger(DomainServiceTest.class);

    @BeforeEach
    public void create() {
        logger.info("##### Spring security TEST_ACCOUNT 인증 #####");
        User user = new User("TEST_USER1");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        logger.info("##### TEST_DOMAIN 생성하기 #####");
        Domain domain = new Domain();
        domain.setDomainId("TEST_DOMAIN");
        domain.setIsEnable("Y");
        domain.setMessage(new Message("PROP_TYPE_STRN"));
        domainService.save(domain);
    }

    @Test
    public void getList() {
        List<Domain> domainList = domainService.getList();
        assertNotNull(domainList);
    }

    @Test
    public void save() {
        logger.info("##### TEST_DOMAIN 가져오기 #####");
        Domain domain = domainService.getObject("TEST_DOMAIN");
        logger.info(domain.toString());

        logger.info("##### TEST_DOMAIN NULL CHECK #####");
        assertNotNull(domain.getDomainId());

        logger.info("##### TEST_DOMAIN 값 업데이트 #####");
        domain.setIsEnable("Y");
        domainService.save(domain);

        logger.info("##### TEST_DOMAIN 데이터 확인 #####");
        Domain checkDomain = domainService.getObject("TEST_DOMAIN");
        assertEquals(checkDomain.getIsEnable(), "Y");
    }

    @Test
    public void getObject() {
        logger.info("##### TEST_DOMAIN 가져오기 #####");
        Domain domain = domainService.getObject("TEST_DOMAIN");

        logger.info("##### TEST_DOMAIN value check #####");
        assertEquals("TEST_DOMAIN", domain.getDomainId());
        assertEquals("Y", domain.getIsEnable());
        assertEquals("PROP_TYPE_STRN", domain.getMessage().getMessageId());
    }

    @AfterEach
    public void delete() {
        logger.info("##### TEST_DOMAIN 삭제 #####");
        domainService.delete("TEST_DOMAIN");
    }
}
