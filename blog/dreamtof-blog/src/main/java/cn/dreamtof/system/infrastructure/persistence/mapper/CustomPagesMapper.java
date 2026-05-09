package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.CustomPagesPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.CustomPagesPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 自定义页面表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CustomPagesPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    CustomPagesPOPO toPO(CustomPagesPO entity);

    /**
     * PO 转 Entity (出库)
     */
    CustomPagesPO toEntity(CustomPagesPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<CustomPagesPO> toEntityList(List<CustomPagesPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<CustomPagesPOPO> toPOList(List<CustomPagesPO> entityList);
}