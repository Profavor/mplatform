package com.favorsoft.mplatform.cdn.projection;

import com.favorsoft.mplatform.cdn.domain.Mclass;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "mclass", types = { Mclass.class })
public interface MclassProjection {
    String getClassId();
    String getDomainId();
    String getParentId();
    List<ClassPropProjection> getClassProp();
}