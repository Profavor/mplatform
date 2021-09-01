package com.favorsoft.mplatform.cdn.web.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.favorsoft.mplatform.cdn.dto.PropValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.favorsoft.mplatform.exception.MplatformErrorCode;
import com.favorsoft.mplatform.exception.MplatformRuntimeException;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;

@RestController
@RequestMapping("/exception-test")
public class ExceptionTestController {

    @RequestMapping(value="/test1", method=RequestMethod.GET)
    public Map<String, String> jsonTest1() {
        Map<String, String> returnMap = new HashMap<String, String>();
        returnMap.put("name", "ajh");
        returnMap.put("age", "30");
        return returnMap;
    }

    @RequestMapping(value="/test2", method=RequestMethod.GET)
    public MasterDTO jsonTest2() {
    	Map<String, Object> testMap = new HashMap<String, Object>();
    	testMap.put("name", "ajh");
        testMap.put("age", "30");

        List<PropValue> data = new ArrayList<>();

    	MasterDTO masterDto = new MasterDTO("testId", data);

        return masterDto;
    }

    @RequestMapping(value="/rte/{isError}", method=RequestMethod.GET)
    public String runTimeExceptionTest(@PathVariable("isError") String isError){
    	if (isError.contentEquals("true")) {
    		String errorMsg = "Test Custom Exception DataBase Error";
    		throw new MplatformRuntimeException(MplatformErrorCode.DATA_ACCESS_ERROR, errorMsg);
    	}
        return "noError";
    }

}
