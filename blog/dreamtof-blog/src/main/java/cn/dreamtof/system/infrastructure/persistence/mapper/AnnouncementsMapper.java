package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.AnnouncementsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.AnnouncementsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 公告表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AnnouncementsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    AnnouncementsPOPO toPO(AnnouncementsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    AnnouncementsPO toEntity(AnnouncementsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<AnnouncementsPO> toEntityList(List<AnnouncementsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<AnnouncementsPOPO> toPOList(List<AnnouncementsPO> entityList);
}