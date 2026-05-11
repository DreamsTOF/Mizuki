package cn.dreamtof.device.application.assembler;

import cn.dreamtof.device.domain.model.entity.Devices;
import cn.dreamtof.device.infrastructure.persistence.po.DevicesPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 设备表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface DevicesAssembler {

    /**
     * Entity 转 PO (入库)
     */
    DevicesPO toPO(Devices entity);

    /**
     * PO 转 Entity (出库)
     */
    Devices toEntity(DevicesPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Devices> toEntityList(List<DevicesPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<DevicesPO> toPOList(List<Devices> entityList);
}