package cn.dreamtof.social.application.assembler;

import cn.dreamtof.blog.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.infrastructure.persistence.po.FriendTagsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 友链标签表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface FriendTagsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    FriendTagsPO toPO(FriendTags entity);

    /**
     * PO 转 Entity (出库)
     */
    FriendTags toEntity(FriendTagsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<FriendTags> toEntityList(List<FriendTagsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<FriendTagsPO> toPOList(List<FriendTags> entityList);
}