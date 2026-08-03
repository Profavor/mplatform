package com.classification.domain_system.service;

import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
public class DqRuleEngineReproductionTest {

    @Autowired
    private DqRuleEngine dqRuleEngine;

    @Test
    public void reproduce500Error() {
        UUID nodeId = UUID.fromString("5968bd8f-509f-40cd-a299-68981da55b6f");
        UUID recordId = UUID.fromString("940e09f7-af0b-4d13-a1ce-475d4b2e2ca7");
        String jsonPayload = "{ \"emp_no\": \"0000001\", \"name\": {\"ko\": \"TestName\"}, \"join_date\": \"2022-05-07\", \"resident_no\": \"880104-1234567\" }";

        try {
            var result = dqRuleEngine.evaluate(nodeId, jsonPayload, recordId);
            System.out.println("Result valid: " + result.isValid());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
