package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.SiteConfigVO;
import cn.dreamtof.system.domain.model.entity.SiteConfigs;
import cn.dreamtof.system.infrastructure.persistence.po.SiteConfigsPO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SiteConfigsAssembler {

    SiteConfigsPO toPO(SiteConfigs entity);

    SiteConfigs toEntity(SiteConfigsPO po);

    List<SiteConfigs> toEntityList(List<SiteConfigsPO> poList);

    List<SiteConfigsPO> toPOList(List<SiteConfigs> entityList);

    SiteConfigVO toVO(SiteConfigs entity);

    List<SiteConfigVO> toVOList(List<SiteConfigs> entities);
}
