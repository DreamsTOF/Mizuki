package cn.dreamtof.content.application.service;

import cn.dreamtof.content.api.vo.ArchiveVO;
import cn.dreamtof.content.application.assembler.ArchivesAssembler;
import cn.dreamtof.content.domain.model.entity.Archives;
import cn.dreamtof.content.domain.service.ArchiveDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchivesAppService {

    private final ArchiveDomainService archiveDomainService;
    private final ArchivesAssembler assembler;

    public void rebuildArchives() {
        archiveDomainService.rebuildArchives();
        log.info("归档索引重建完成");
    }

    public List<ArchiveVO> listByYear(Integer year) {
        List<Archives> entities = archiveDomainService.listByYear(year);
        return assembler.toVOList(entities);
    }

    public List<ArchiveVO> listAll() {
        List<Archives> entities = archiveDomainService.listAll();
        return assembler.toVOList(entities);
    }
}
