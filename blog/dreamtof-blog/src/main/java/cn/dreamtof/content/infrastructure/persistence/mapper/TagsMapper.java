package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.TagsPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.TagsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 标签表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TagsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TagsPOPO toPO(TagsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    TagsPO toEntity(TagsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TagsPO> toEntityList(List<TagsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TagsPOPO> toPOList(List<TagsPO> entityList);
}