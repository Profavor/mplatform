package com.favorsoft.mplatform.cdn.service;

import com.favorsoft.mplatform.cdn.domain.*;
import com.favorsoft.mplatform.cdn.domain.keys.ClassPropKey;
import com.favorsoft.mplatform.cdn.domain.keys.MclassKey;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
public class MasterServiceTest {
    private static Logger logger = LoggerFactory.getLogger(MasterServiceTest.class);

    @Autowired
    private MclassService mclassService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private PropService propService;

    @Autowired
    private ClassPropService classPropService;

    @Autowired
    private MasterService masterService;

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

        logger.info("##### ROOT CLASS 생성하기 #####");
        Mclass rootClass = new Mclass();
        rootClass.setClassId("ROOT");
        rootClass.setDomainId("TEST_DOMAIN");
        rootClass.setIsEnable("Y");
        rootClass.setMessage(new Message("PROP_TYPE_FILE"));
        mclassService.save(rootClass);

        logger.info("##### LEAF CLASS 생성하기 #####");
        Mclass leafClass = new Mclass();
        leafClass.setClassId("LEAF");
        leafClass.setDomainId("TEST_DOMAIN");
        leafClass.setIsEnable("Y");
        leafClass.setMessage(new Message("PROP_TYPE_EDITOR"));
        leafClass.setParentId("ROOT");
        mclassService.save(leafClass);

        logger.info("##### TEST_PROP1 속성 생성 #####");
        Prop prop1 = new Prop();
        prop1.setPropId("TEST_PROP1");
        prop1.setArea(new Area("*"));
        prop1.setMgroup(new Mgroup("STANDARD"));
        prop1.setDbType("VARCHAR(100)");
        prop1.setMessage(new Message("PROP_TYPE_DATE"));
        prop1.setWidth(100);
        prop1.setPropType(new PropType("STRN"));
        propService.save(prop1);

        logger.info("##### TEST_PROP2 속성 생성 #####");
        Prop prop2 = new Prop();
        prop2.setPropId("TEST_PROP2");
        prop2.setArea(new Area("*"));
        prop2.setMgroup(new Mgroup("STANDARD"));
        prop2.setDbType("VARCHAR(100)");
        prop2.setMessage(new Message("PROP_TYPE_DATE"));
        prop2.setWidth(100);
        prop2.setPropType(new PropType("STRN"));
        propService.save(prop2);

        logger.info("##### ROOT CLASS 속성 할당(TEST_PROP1) #####");
        ClassProp classProp1 = new ClassProp();
        classProp1.setClassId("ROOT");
        classProp1.setDomainId("TEST_DOMAIN");
        classProp1.setPropId("TEST_PROP1");
        classProp1.setDispSeq(21);
        classProp1.setIsShow("Y");
        classProp1.setIsDisabled("N");
        classProp1.setIsReadOnly("N");
        classPropService.save(classProp1);

        logger.info("##### LEAF CLASS 속성 할당(TEST_PROP2) #####");
        ClassProp classProp2 = new ClassProp();
        classProp2.setClassId("LEAF");
        classProp2.setDomainId("TEST_DOMAIN");
        classProp2.setPropId("TEST_PROP2");
        classProp2.setDispSeq(1);
        classProp2.setIsShow("Y");
        classProp2.setIsDisabled("N");
        classProp2.setIsReadOnly("N");
        classPropService.save(classProp2);
    }

    @AfterEach
    public void delete() {
        logger.info("##### ROOT CLASS 속성 할당 제거 #####");
        classPropService.delete(new ClassPropKey("ROOT", "TEST_DOMAIN", "TEST_PROP1"));

        logger.info("##### LEAF CLASS 속성 할당 제거 #####");
        classPropService.delete(new ClassPropKey("LEAF", "TEST_DOMAIN", "TEST_PROP2"));

        logger.info("##### TEST_PROP1 속성 제거 #####");
        propService.delete("TEST_PROP1");

        logger.info("##### LEAF CLASS 제거 #####");
        mclassService.delete(new MclassKey("TEST_DOMAIN", "LEAF"));

        logger.info("##### ROOT CLASS 제거 #####");
        mclassService.delete(new MclassKey("TEST_DOMAIN", "ROOT"));

        logger.info("##### TEST_DOMAIN 제거 #####");
        domainService.delete("TEST_DOMAIN");

    }

    @Test
    public void saveMaster(){
        logger.info("##### SAVE MASTER #####");
        MasterDTO masterDTO = new MasterDTO();
        masterDTO.setDomainId("TEST_DOMAIN");
        masterDTO.setClassId("LEAF");

        masterDTO.setMasterId("MASTER_KEY");
        masterDTO.setStatus("USE");

        List<PropValue> propValues = new ArrayList<>();
        PropValue pv1 = PropValue.builder()
                .propId("TEST_PROP1")
                .masterId("MASTER_KEY")
                .areaId("*")
                .type("STRN")
                .value("Test data1")
                .build();
        propValues.add(pv1);

        PropValue pv2 = PropValue.builder()
                .propId("TEST_PROP2")
                .masterId("MASTER_KEY")
                .areaId("*")
                .type("STRN")
                .value("Test data2")
                .build();
        propValues.add(pv2);

        masterDTO.setData(propValues);

        masterService.saveMaster(masterDTO);

        logger.info("##### SAVE DATA CHECK #####");
        List<Map<String, Object>> data = masterService.getMasterData(masterDTO);
        logger.info(data.toString());
        assertEquals(data.get(0).get("TEST_PROP1"), "Test data1");
        assertEquals(data.get(0).get("TEST_PROP2"), "Test data2");
    }


}
