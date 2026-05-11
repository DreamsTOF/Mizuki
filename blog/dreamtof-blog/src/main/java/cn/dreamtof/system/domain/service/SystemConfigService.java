package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.SiteConfigs;
import cn.dreamtof.system.domain.repository.SiteConfigsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SiteConfigsRepository repository;

    public SiteConfigs getByKey(String configKey) {
        Asserts.notBlank(configKey, "配置项键名不能为空");
        return repository.findByKey(configKey);
    }

    public String getValue(String configKey) {
        SiteConfigs config = getByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    public List<SiteConfigs> listByGroup(String groupPrefix) {
        Asserts.notBlank(groupPrefix, "配置组前缀不能为空");
        return repository.listByKeyPrefix(groupPrefix);
    }

    public List<SiteConfigs> listAll() {
        return repository.listAll();
    }

    public Map<String, String> getAllAsMap() {
        List<SiteConfigs> configs = repository.listAll();
        return configs.stream()
                .collect(java.util.stream.Collectors.toMap(SiteConfigs::getConfigKey, SiteConfigs::getConfigValue, (a, b) -> b));
    }

    public SiteConfigs updateByKey(String configKey, String configValue) {
        Asserts.notBlank(configKey, "配置项键名不能为空");
        Asserts.notBlank(configValue, "配置项值不能为空");
        SiteConfigs config = repository.findByKey(configKey);
        Asserts.notNull(config, "配置项不存在: " + configKey);
        config.updateValue(configValue);
        return repository.update(config);
    }

    public void batchUpdate(Map<String, String> configMap) {
        Asserts.isTrue(configMap != null && !configMap.isEmpty(), "配置项不能为空");
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            SiteConfigs config = repository.findByKey(entry.getKey());
            if (config != null) {
                config.updateValue(entry.getValue());
                repository.update(config);
            }
        }
    }

    public SiteConfigs create(String configKey, String configValue, String description) {
        SiteConfigs existing = repository.findByKey(configKey);
        Asserts.isTrue(existing == null, "配置项已存在: " + configKey);
        SiteConfigs entity = SiteConfigs.create(configKey, configValue, description);
        return repository.create(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }
}
