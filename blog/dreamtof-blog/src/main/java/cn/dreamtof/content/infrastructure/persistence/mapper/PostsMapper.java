package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.PostsPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.PostsPOPO;
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
public interface PostsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PostsPOPO toPO(PostsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    PostsPO toEntity(PostsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<PostsPO> toEntityList(List<PostsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PostsPOPO> toPOList(List<PostsPO> entityList);
}