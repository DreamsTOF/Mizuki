package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.BannersPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.BannersPOPO;
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
public interface BannersPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    BannersPOPO toPO(BannersPO entity);

    /**
     * PO 转 Entity (出库)
     */
    BannersPO toEntity(BannersPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<BannersPO> toEntityList(List<BannersPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<BannersPOPO> toPOList(List<BannersPO> entityList);
}