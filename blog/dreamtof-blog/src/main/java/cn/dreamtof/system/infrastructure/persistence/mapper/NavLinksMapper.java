package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.NavLinksPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.NavLinksPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 导航链接表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface NavLinksPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    NavLinksPOPO toPO(NavLinksPO entity);

    /**
     * PO 转 Entity (出库)
     */
    NavLinksPO toEntity(NavLinksPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<NavLinksPO> toEntityList(List<NavLinksPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<NavLinksPOPO> toPOList(List<NavLinksPO> entityList);
}