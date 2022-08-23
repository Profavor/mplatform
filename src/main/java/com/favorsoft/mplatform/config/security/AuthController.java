package com.favorsoft.mplatform.config.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/auth")
public class AuthController {

    @RequestMapping("/denied")
    @ResponseBody
    public String denied(){
        return "Access denied!!";
    }
    
}
