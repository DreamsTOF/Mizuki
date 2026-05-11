package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventLinks;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventLinksPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 时间线链接关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TimelineEventLinksAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventLinksPO toPO(TimelineEventLinks entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventLinks toEntity(TimelineEventLinksPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventLinks> toEntityList(List<TimelineEventLinksPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventLinksPO> toPOList(List<TimelineEventLinks> entityList);
}