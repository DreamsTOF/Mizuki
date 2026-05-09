package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.PostTags;
import cn.dreamtof.content.infrastructure.persistence.po.PostTagsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 文章-标签关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface PostTagsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PostTagsPO toPO(PostTags entity);

    /**
     * PO 转 Entity (出库)
     */
    PostTags toEntity(PostTagsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<PostTags> toEntityList(List<PostTagsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PostTagsPO> toPOList(List<PostTags> entityList);
}