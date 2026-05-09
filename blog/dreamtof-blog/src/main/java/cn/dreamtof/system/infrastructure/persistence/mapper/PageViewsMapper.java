package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.PageViewsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.PageViewsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 页面访问统计表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface PageViewsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PageViewsPOPO toPO(PageViewsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    PageViewsPO toEntity(PageViewsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<PageViewsPO> toEntityList(List<PageViewsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PageViewsPOPO> toPOList(List<PageViewsPO> entityList);
}