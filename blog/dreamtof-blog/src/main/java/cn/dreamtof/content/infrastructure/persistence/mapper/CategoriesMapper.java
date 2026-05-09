package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.CategoriesPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.CategoriesPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 文章分类表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CategoriesPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    CategoriesPOPO toPO(CategoriesPO entity);

    /**
     * PO 转 Entity (出库)
     */
    CategoriesPO toEntity(CategoriesPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<CategoriesPO> toEntityList(List<CategoriesPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<CategoriesPOPO> toPOList(List<CategoriesPO> entityList);
}