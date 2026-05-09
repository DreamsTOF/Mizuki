package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.ThemeSettings;
import cn.dreamtof.system.infrastructure.persistence.po.ThemeSettingsPO;
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
public interface ThemeSettingsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ThemeSettingsPO toPO(ThemeSettings entity);

    /**
     * PO 转 Entity (出库)
     */
    ThemeSettings toEntity(ThemeSettingsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ThemeSettings> toEntityList(List<ThemeSettingsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ThemeSettingsPO> toPOList(List<ThemeSettings> entityList);
}