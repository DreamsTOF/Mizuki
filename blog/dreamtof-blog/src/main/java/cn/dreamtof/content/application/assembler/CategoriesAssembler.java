package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.Categories;
import cn.dreamtof.content.infrastructure.persistence.po.CategoriesPO;
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
public interface CategoriesAssembler {

    /**
     * Entity 转 PO (入库)
     */
    CategoriesPO toPO(Categories entity);

    /**
     * PO 转 Entity (出库)
     */
    Categories toEntity(CategoriesPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Categories> toEntityList(List<CategoriesPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<CategoriesPO> toPOList(List<Categories> entityList);
}