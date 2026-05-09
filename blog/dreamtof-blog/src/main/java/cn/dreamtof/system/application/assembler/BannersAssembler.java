package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.Banners;
import cn.dreamtof.system.infrastructure.persistence.po.BannersPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 横幅图片表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface BannersAssembler {

    /**
     * Entity 转 PO (入库)
     */
    BannersPO toPO(Banners entity);

    /**
     * PO 转 Entity (出库)
     */
    Banners toEntity(BannersPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Banners> toEntityList(List<BannersPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<BannersPO> toPOList(List<Banners> entityList);
}