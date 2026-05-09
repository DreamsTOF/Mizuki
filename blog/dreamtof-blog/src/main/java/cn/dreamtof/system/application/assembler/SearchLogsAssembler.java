package cn.dreamtof.system.application.assembler;

import cn.dreamtof.blog.system.domain.model.entity.SearchLogs;
import cn.dreamtof.system.infrastructure.persistence.po.SearchLogsPO;
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
public interface SearchLogsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    SearchLogsPO toPO(SearchLogs entity);

    /**
     * PO 转 Entity (出库)
     */
    SearchLogs toEntity(SearchLogsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<SearchLogs> toEntityList(List<SearchLogsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<SearchLogsPO> toPOList(List<SearchLogs> entityList);
}