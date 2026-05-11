package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventAchievementsPO;
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
public interface TimelineEventAchievementsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventAchievementsPO toPO(TimelineEventAchievements entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventAchievements toEntity(TimelineEventAchievementsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventAchievements> toEntityList(List<TimelineEventAchievementsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventAchievementsPO> toPOList(List<TimelineEventAchievements> entityList);
}