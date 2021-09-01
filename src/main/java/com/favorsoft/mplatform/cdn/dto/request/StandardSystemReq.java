package com.favorsoft.mplatform.cdn.dto.request;

import com.favorsoft.mplatform.cdn.enums.Language;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class StandardSystemReq {
    private String domainId;
    private String classId;
    private Language lang = Language.KO;

    public void setLang(String lang) {
        try {
            this.lang = StringUtils.hasText(lang) ? Language.valueOf(lang.toUpperCase()): Language.KO;
        } catch (IllegalArgumentException e) {
            this.lang = Language.KO;
        }
    }
}
