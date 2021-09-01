package com.favorsoft.mplatform.cdn.dto;

import lombok.Data;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * 화면에서 사용할 속성정보를 Tree 구조 데이터로 변환 (JsonObject)
 *
 */
@Data
@Component
public class PropDTO {

    private String json;

    public PropDTO() { }

    public PropDTO(String domainId, String classId) {
        json = makeJson(domainId, classId).toJSONString();
    }

    private JSONObject makeJson(String domainId, String classId){
        JSONObject domain = new JSONObject();
        domain.put("domainId", "domainId");

        JSONObject mclass = new JSONObject();
        mclass.put("classId", "classId");

        JSONArray sections = new JSONArray();

        JSONObject msection = new JSONObject();
        msection.put("sectionId", "sectionId");

        JSONArray groups = new JSONArray();

        JSONObject mgroup = new JSONObject();
        mgroup.put("groupId1", "groupId1");

        JSONArray props = new JSONArray();

        JSONObject prop = new JSONObject();
        prop.put("propId", "test");

        JSONObject classProp = new JSONObject();
        prop.appendField("classProp", classProp);

        props.add(prop);
        mgroup.put("props", props);

        groups.add(mgroup);

        msection.put("groups", groups);

        sections.add(msection);

        mclass.appendField("sections", sections);
        domain.appendField("mclass", mclass);

        return domain;
    }

//     1. classId에 상위 classId들을 가져온다.
//     2. classId에 맵핑된 propId들을 가져온다.
//      (만약 상위클래스에 포함된 propId와 하위 클래스에 포함된 propId가 모두 존재시 하위 클래스에 포함된 propId로 적용한다.(override) => ClassProp 옵션)
//
//     Domain <Domain> # 도메인 정보를 가져온다.
//     Class <MClass>  # 상위 분류 클래스에 포함되어 있는 속성들을 전부 가져온다. (상속)
//     Section List<Msection> # 그룹들이 가지고 있는 섹션들을 가져온다. (Display Order 적용)
//       -Group1 List<Mgroup> # 속성들이 가지고 있는 그룹들을 가져온다. (Display Order 적용)
//           Prop1 List<Prop> # 속성을 그룹에 맵핑한다. (Display Order 적용)
//               options <ClassProp> # 분류별 속성에 대한 옵션값을 가져온다.
//           Prop2
//               options
//           Prop3
//               options
//      -Group2
//           Prop1
//               options
//           Prop2
//               options
//
//     JSON Sampling
//
//     {   "domain": { "domainId": "test" },
//         "mclass" : { "classId": "test1234" },
//         "msection" : [
//              {
//                  "sectionId": "sec",
//                  "mgroup": [
//                         {
//                             "groupId": "group1"
//                             "props": [
//                                  {
//                                      "propId": "TEST1",
//                                      "type": "TEXT",
//                                      "options": {
//                                           "isRead": true
//                                      }
//                                  },
//                                  {
//                                      "propId": "TEST2",
//                                      "options": {
//                                           "isRead": false
//                                      }
//                                  },
//
//                             ]
//                         },
//                         {"groupId": "group2"}
//                  ]
//              },
//              {"sectionId": "sec2"}
//         ]
//
//     }
}
