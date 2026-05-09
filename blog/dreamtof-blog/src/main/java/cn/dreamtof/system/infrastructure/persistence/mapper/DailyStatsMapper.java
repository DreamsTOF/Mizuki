package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.DailyStatsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.DailyStatsPOPO;
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
public interface DailyStatsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DailyStatsPOPO toPO(DailyStatsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    DailyStatsPO toEntity(DailyStatsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<DailyStatsPO> toEntityList(List<DailyStatsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DailyStatsPOPO> toPOList(List<DailyStatsPO> entityList);
}