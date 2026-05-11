package cn.dreamtof.device.application.assembler;

import cn.dreamtof.device.domain.model.entity.DeviceCategories;
import cn.dreamtof.device.infrastructure.persistence.po.DeviceCategoriesPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 设备分类表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface DeviceCategoriesAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DeviceCategoriesPO toPO(DeviceCategories entity);

    /**
     * PO 转 Entity (出库)
     */
    DeviceCategories toEntity(DeviceCategoriesPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<DeviceCategories> toEntityList(List<DeviceCategoriesPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DeviceCategoriesPO> toPOList(List<DeviceCategories> entityList);
}