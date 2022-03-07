package com.favorsoft.mplatform.cdn.domain;

import com.favorsoft.mplatform.cdn.dto.PropDTO2;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Prop extends BaseEntity {

    public Prop(String propId){
        this.propId = propId;
    }

    @Id
    @Column(length = 100)
    private String propId;

    @Column(length = 200)
    private String unit;

    @Column(length = 200)
    private int width;

    @Column(length = 200)
    private String regex;

    @Column(length = 200)
    private String ruleCode;

    @Column(length = 200)
    private String mask;

    @Column(length = 200)
    private String reference;

    @NotNull
    @Column(length = 50)
    private String dbType = "VARCHAR(200)";

    @ManyToOne
    @JoinColumn(name= "areaId")
    @RestResource(path = "area", rel="area", exported = false)
    private Area area;

    @ManyToOne
    @JoinColumn(name= "type")
    @RestResource(path = "propType", rel="propType", exported = false)
    private PropType propType;

    @ManyToOne
    @JoinColumn(name="messageId")
    @RestResource(path = "message", rel="message", exported = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name="groupId")
    @RestResource(path = "mgroup", rel="mgroup", exported = false)
    private Mgroup mgroup;

    @Builder
    public Prop(String propId, String unit, int width, String regex, String ruleCode, String dbType, String areaId, String type, String messageId, String groupId, String reference){
        this.propId = propId;
        this.unit = unit;
        this.width = width;
        this.regex = regex;
        this.ruleCode = ruleCode;
        this.dbType = dbType;
        this.area = new Area(areaId);
        this.propType = new PropType(type);
        this.message = new Message(messageId);
        this.mgroup = new Mgroup(groupId);
        this.reference = reference;
    }

    public static Prop.PropBuilder fromPropDTO(PropDTO2 propDTO){
        return Prop.builder()
                .propId(propDTO.getPropId())
                .unit(propDTO.getUnit())
                .width(propDTO.getWidth())
                .regex(propDTO.getRegex())
                .ruleCode(propDTO.getRuleCode())
                .dbType(propDTO.getDbType())
                .areaId(propDTO.getAreaId())
                .type(propDTO.getType())
                .messageId(propDTO.getMessageId())
                .groupId(propDTO.getGroupId())
                .reference(propDTO.getReference());
    }
}
