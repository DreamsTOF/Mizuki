package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.NavLinkVO;
import cn.dreamtof.system.domain.model.entity.NavLinks;
import cn.dreamtof.system.infrastructure.persistence.po.NavLinksPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NavLinksAssembler {

    NavLinksPO toPO(NavLinks entity);

    NavLinks toEntity(NavLinksPO po);

    List<NavLinks> toEntityList(List<NavLinksPO> poList);

    List<NavLinksPO> toPOList(List<NavLinks> entityList);

    NavLinkVO toVO(NavLinks entity);

    List<NavLinkVO> toVOList(List<NavLinks> entities);
}
