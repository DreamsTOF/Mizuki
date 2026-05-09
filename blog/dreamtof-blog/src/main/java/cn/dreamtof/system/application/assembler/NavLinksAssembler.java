package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.NavLinks;
import cn.dreamtof.system.infrastructure.persistence.po.NavLinksPO;
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
public interface NavLinksAssembler {

    /**
     * Entity 转 PO (入库)
     */
    NavLinksPO toPO(NavLinks entity);

    /**
     * PO 转 Entity (出库)
     */
    NavLinks toEntity(NavLinksPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<NavLinks> toEntityList(List<NavLinksPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<NavLinksPO> toPOList(List<NavLinks> entityList);
}