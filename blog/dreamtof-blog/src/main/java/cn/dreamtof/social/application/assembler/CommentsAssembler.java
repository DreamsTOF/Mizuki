package cn.dreamtof.social.application.assembler;

import cn.dreamtof.blog.social.domain.model.entity.Comments;
import cn.dreamtof.social.infrastructure.persistence.po.CommentsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 评论表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CommentsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    CommentsPO toPO(Comments entity);

    /**
     * PO 转 Entity (出库)
     */
    Comments toEntity(CommentsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Comments> toEntityList(List<CommentsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<CommentsPO> toPOList(List<Comments> entityList);
}