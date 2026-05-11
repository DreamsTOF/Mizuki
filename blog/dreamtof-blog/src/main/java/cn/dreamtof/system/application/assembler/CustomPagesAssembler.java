package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.CustomPageVO;
import cn.dreamtof.system.domain.model.entity.CustomPages;
import cn.dreamtof.system.infrastructure.persistence.po.CustomPagesPO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomPagesAssembler {

    CustomPagesPO toPO(CustomPages entity);

    CustomPages toEntity(CustomPagesPO po);

    List<CustomPages> toEntityList(List<CustomPagesPO> poList);

    List<CustomPagesPO> toPOList(List<CustomPages> entityList);

    CustomPageVO toVO(CustomPages entity);

    List<CustomPageVO> toVOList(List<CustomPages> entities);
}
