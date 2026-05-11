package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.portfolio.api.vo.TimelineEventVO;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEvents;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimelineEventsAssembler {

    TimelineEventsPO toPO(TimelineEvents entity);

    TimelineEvents toEntity(TimelineEventsPO po);

    List<TimelineEvents> toEntityList(List<TimelineEventsPO> poList);

    List<TimelineEventsPO> toPOList(List<TimelineEvents> entityList);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    TimelineEventVO toVO(TimelineEvents entity);

    List<TimelineEventVO> toVOList(List<TimelineEvents> entities);
}
