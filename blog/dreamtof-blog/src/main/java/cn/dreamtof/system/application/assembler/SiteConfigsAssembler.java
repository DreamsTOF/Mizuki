package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.SiteConfigs;
import cn.dreamtof.system.infrastructure.persistence.po.SiteConfigsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 站点配置表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface SiteConfigsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SiteConfigsPO toPO(SiteConfigs entity);

    /**
     * PO 转 Entity (出库)
     */
    SiteConfigs toEntity(SiteConfigsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<SiteConfigs> toEntityList(List<SiteConfigsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SiteConfigsPO> toPOList(List<SiteConfigs> entityList);
}