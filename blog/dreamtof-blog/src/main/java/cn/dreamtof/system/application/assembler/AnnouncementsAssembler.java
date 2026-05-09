package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.Announcements;
import cn.dreamtof.system.infrastructure.persistence.po.AnnouncementsPO;
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
public interface AnnouncementsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    AnnouncementsPO toPO(Announcements entity);

    /**
     * PO 转 Entity (出库)
     */
    Announcements toEntity(AnnouncementsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Announcements> toEntityList(List<AnnouncementsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<AnnouncementsPO> toPOList(List<Announcements> entityList);
}