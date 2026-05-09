package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.DiaryEntries;
import cn.dreamtof.content.infrastructure.persistence.po.DiaryEntriesPO;
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
public interface DiaryEntriesAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DiaryEntriesPO toPO(DiaryEntries entity);

    /**
     * PO 转 Entity (出库)
     */
    DiaryEntries toEntity(DiaryEntriesPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<DiaryEntries> toEntityList(List<DiaryEntriesPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DiaryEntriesPO> toPOList(List<DiaryEntries> entityList);
}