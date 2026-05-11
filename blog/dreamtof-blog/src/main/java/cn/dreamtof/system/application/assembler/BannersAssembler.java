package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.BannerVO;
import cn.dreamtof.system.domain.model.entity.Banners;
import cn.dreamtof.system.infrastructure.persistence.po.BannersPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BannersAssembler {

    BannersPO toPO(Banners entity);

    Banners toEntity(BannersPO po);

    List<Banners> toEntityList(List<BannersPO> poList);

    List<BannersPO> toPOList(List<Banners> entityList);

    BannerVO toVO(Banners entity);

    List<BannerVO> toVOList(List<Banners> entities);
}
