package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.PageViews;
import cn.dreamtof.system.infrastructure.persistence.po.PageViewsPO;
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
public interface PageViewsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    PageViewsPO toPO(PageViews entity);

    /**
     * PO 转 Entity (出库)
     */
    PageViews toEntity(PageViewsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<PageViews> toEntityList(List<PageViewsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<PageViewsPO> toPOList(List<PageViews> entityList);
}