package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.Posts;
import cn.dreamtof.content.infrastructure.persistence.po.PostsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 文章主表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface PostsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PostsPO toPO(Posts entity);

    /**
     * PO 转 Entity (出库)
     */
    Posts toEntity(PostsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Posts> toEntityList(List<PostsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PostsPO> toPOList(List<Posts> entityList);
}