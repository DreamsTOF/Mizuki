package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.SkillsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.SkillsPOPO;
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
public interface SkillsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SkillsPOPO toPO(SkillsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    SkillsPO toEntity(SkillsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<SkillsPO> toEntityList(List<SkillsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SkillsPOPO> toPOList(List<SkillsPO> entityList);
}