package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.api.vo.SiteConfigVO;
import cn.dreamtof.system.application.assembler.SiteConfigsAssembler;
import cn.dreamtof.system.domain.model.entity.SiteConfigs;
import cn.dreamtof.system.domain.repository.SiteConfigsRepository;
import cn.dreamtof.system.domain.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteConfigsAppService {

    private final SiteConfigsRepository repository;
    private final SystemConfigService configService;
    private final SiteConfigsAssembler assembler;

    public SiteConfigVO getByKey(String configKey) {
        SiteConfigs entity = configService.getByKey(configKey);
        return assembler.toVO(entity);
    }

    public String getValue(String configKey) {
        return configService.getValue(configKey);
    }

    public List<SiteConfigVO> listByGroup(String groupPrefix) {
        List<SiteConfigs> entities = configService.listByGroup(groupPrefix);
        return assembler.toVOList(entities);
    }

    public List<SiteConfigVO> listAll() {
        List<SiteConfigs> entities = configService.listAll();
        return assembler.toVOList(entities);
    }

    public Map<String, String> getAllAsMap() {
        return configService.getAllAsMap();
    }

    public SiteConfigVO updateByKey(String configKey, String configValue) {
        SiteConfigs entity = configService.updateByKey(configKey, configValue);
        return assembler.toVO(entity);
    }

    public void batchUpdate(Map<String, String> configMap) {
        configService.batchUpdate(configMap);
    }

    public SiteConfigVO create(String configKey, String configValue, String description) {
        SiteConfigs entity = configService.create(configKey, configValue, description);
        return assembler.toVO(entity);
    }

    public boolean removeById(UUID id) {
        return configService.removeById(id);
    }

    public PageResult<SiteConfigVO> page(PageReq pageReq) {
        PageResult<SiteConfigs> result = repository.page(pageReq);
        List<SiteConfigVO> voList = assembler.toVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), result.getPages(), result.getPageNum(), result.getPageSize());
    }

    public CursorResult<SiteConfigVO> seek(UUID cursor, int limit) {
        CursorResult<SiteConfigs> result = repository.seek(cursor, limit);
        List<SiteConfigVO> voList = assembler.toVOList(result.getRecords());
        return new CursorResult<>(voList, result.getNextCursor(), result.isHasNext());
    }
}
