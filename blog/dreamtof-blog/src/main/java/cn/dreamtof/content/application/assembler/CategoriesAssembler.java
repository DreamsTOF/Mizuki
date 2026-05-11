package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.api.vo.CategoryVO;
import cn.dreamtof.content.domain.model.entity.Categories;
import cn.dreamtof.content.infrastructure.persistence.po.CategoriesPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriesAssembler {

    CategoriesPO toPO(Categories entity);

    Categories toEntity(CategoriesPO po);

    List<Categories> toEntityList(List<CategoriesPO> poList);

    List<CategoriesPO> toPOList(List<Categories> entityList);

    CategoryVO toVO(Categories entity);

    List<CategoryVO> toVOList(List<Categories> entities);
}
