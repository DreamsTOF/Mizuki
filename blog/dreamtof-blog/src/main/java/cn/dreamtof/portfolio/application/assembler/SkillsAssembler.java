package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.Skills;
import cn.dreamtof.portfolio.infrastructure.persistence.po.SkillsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 技能表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface SkillsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SkillsPO toPO(Skills entity);

    /**
     * PO 转 Entity (出库)
     */
    Skills toEntity(SkillsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Skills> toEntityList(List<SkillsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SkillsPO> toPOList(List<Skills> entityList);
}