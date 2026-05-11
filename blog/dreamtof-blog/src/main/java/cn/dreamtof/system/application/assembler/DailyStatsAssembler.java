package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.domain.model.entity.DailyStats;
import cn.dreamtof.system.infrastructure.persistence.po.DailyStatsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 每日统计汇总表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface DailyStatsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DailyStatsPO toPO(DailyStats entity);

    /**
     * PO 转 Entity (出库)
     */
    DailyStats toEntity(DailyStatsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<DailyStats> toEntityList(List<DailyStatsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DailyStatsPO> toPOList(List<DailyStats> entityList);
}