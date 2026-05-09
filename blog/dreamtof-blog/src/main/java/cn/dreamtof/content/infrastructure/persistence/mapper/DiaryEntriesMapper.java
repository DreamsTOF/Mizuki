package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.DiaryEntriesPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.DiaryEntriesPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 日记条目表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface DiaryEntriesPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DiaryEntriesPOPO toPO(DiaryEntriesPO entity);

    /**
     * PO 转 Entity (出库)
     */
    DiaryEntriesPO toEntity(DiaryEntriesPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<DiaryEntriesPO> toEntityList(List<DiaryEntriesPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DiaryEntriesPOPO> toPOList(List<DiaryEntriesPO> entityList);
}