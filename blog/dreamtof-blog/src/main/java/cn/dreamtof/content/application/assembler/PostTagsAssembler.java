package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.domain.model.entity.PostTags;
import cn.dreamtof.content.infrastructure.persistence.po.PostTagsPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostTagsAssembler {

    PostTagsPO toPO(PostTags entity);

    PostTags toEntity(PostTagsPO po);

    List<PostTags> toEntityList(List<PostTagsPO> poList);

    List<PostTagsPO> toPOList(List<PostTags> entityList);
}
