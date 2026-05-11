package cn.dreamtof.social.application.assembler;

import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import cn.dreamtof.social.infrastructure.persistence.po.FriendTagLinksPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 友链-标签关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface FriendTagLinksAssembler {

    /**
     * Entity 转 PO (入库)
     */
    FriendTagLinksPO toPO(FriendTagLinks entity);

    /**
     * PO 转 Entity (出库)
     */
    FriendTagLinks toEntity(FriendTagLinksPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<FriendTagLinks> toEntityList(List<FriendTagLinksPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<FriendTagLinksPO> toPOList(List<FriendTagLinks> entityList);
}