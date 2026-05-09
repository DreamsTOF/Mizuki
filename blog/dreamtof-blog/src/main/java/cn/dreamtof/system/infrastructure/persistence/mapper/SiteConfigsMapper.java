package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.SiteConfigsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.SiteConfigsPOPO;
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
public interface SiteConfigsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SiteConfigsPOPO toPO(SiteConfigsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    SiteConfigsPO toEntity(SiteConfigsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<SiteConfigsPO> toEntityList(List<SiteConfigsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SiteConfigsPOPO> toPOList(List<SiteConfigsPO> entityList);
}