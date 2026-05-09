package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.ThemeSettingsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.ThemeSettingsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 主题设置表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface ThemeSettingsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ThemeSettingsPOPO toPO(ThemeSettingsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    ThemeSettingsPO toEntity(ThemeSettingsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ThemeSettingsPO> toEntityList(List<ThemeSettingsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ThemeSettingsPOPO> toPOList(List<ThemeSettingsPO> entityList);
}