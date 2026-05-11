package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.ThemeSettingVO;
import cn.dreamtof.system.domain.model.entity.ThemeSettings;
import cn.dreamtof.system.infrastructure.persistence.po.ThemeSettingsPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ThemeSettingsAssembler {

    ThemeSettingsPO toPO(ThemeSettings entity);

    ThemeSettings toEntity(ThemeSettingsPO po);

    List<ThemeSettings> toEntityList(List<ThemeSettingsPO> poList);

    List<ThemeSettingsPO> toPOList(List<ThemeSettings> entityList);

    ThemeSettingVO toVO(ThemeSettings entity);

    List<ThemeSettingVO> toVOList(List<ThemeSettings> entities);
}
