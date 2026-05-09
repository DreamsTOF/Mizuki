package cn.dreamtof.social.application.assembler;

import cn.dreamtof.blog.social.domain.model.entity.Friends;
import cn.dreamtof.social.infrastructure.persistence.po.FriendsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 友链表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface FriendsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    FriendsPO toPO(Friends entity);

    /**
     * PO 转 Entity (出库)
     */
    Friends toEntity(FriendsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Friends> toEntityList(List<FriendsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<FriendsPO> toPOList(List<Friends> entityList);
}