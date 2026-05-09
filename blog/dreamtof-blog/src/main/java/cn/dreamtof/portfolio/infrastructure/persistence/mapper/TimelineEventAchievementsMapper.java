package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventAchievementsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventAchievementsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 时间线成就关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TimelineEventAchievementsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventAchievementsPOPO toPO(TimelineEventAchievementsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventAchievementsPO toEntity(TimelineEventAchievementsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventAchievementsPO> toEntityList(List<TimelineEventAchievementsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventAchievementsPOPO> toPOList(List<TimelineEventAchievementsPO> entityList);
}