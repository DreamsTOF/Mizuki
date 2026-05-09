package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.Tags;
import cn.dreamtof.content.infrastructure.persistence.po.TagsPO;
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
public interface TagsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TagsPO toPO(Tags entity);

    /**
     * PO 转 Entity (出库)
     */
    Tags toEntity(TagsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Tags> toEntityList(List<TagsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TagsPO> toPOList(List<Tags> entityList);
}