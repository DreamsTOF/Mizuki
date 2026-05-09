package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.PostTagsPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.PostTagsPOPO;
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
public interface PostTagsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PostTagsPOPO toPO(PostTagsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    PostTagsPO toEntity(PostTagsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<PostTagsPO> toEntityList(List<PostTagsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PostTagsPOPO> toPOList(List<PostTagsPO> entityList);
}