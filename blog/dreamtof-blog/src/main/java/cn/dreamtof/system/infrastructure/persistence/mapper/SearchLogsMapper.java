package cn.dreamtof.system.infrastructure.persistence.mapper;

import cn.dreamtof.blog.system.infrastructure.persistence.po.SearchLogsPO;
import cn.dreamtof.blog.system.infrastructure.persistence.po.SearchLogsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 搜索记录表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface SearchLogsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SearchLogsPOPO toPO(SearchLogsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    SearchLogsPO toEntity(SearchLogsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<SearchLogsPO> toEntityList(List<SearchLogsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SearchLogsPOPO> toPOList(List<SearchLogsPO> entityList);
}