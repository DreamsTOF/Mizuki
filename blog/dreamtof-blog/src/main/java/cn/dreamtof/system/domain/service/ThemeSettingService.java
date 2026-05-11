package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.ThemeSettings;
import cn.dreamtof.system.domain.repository.ThemeSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeSettingService {

    private final ThemeSettingsRepository themeSettingsRepository;

    public ThemeSettings createSetting(ThemeSettings entity) {
        Asserts.notNull(entity, "主题设置实体不能为空");
        Asserts.isFalse(themeSettingsRepository.existsByKey(entity.getSettingKey()),
                "设置键已存在: " + entity.getSettingKey());
        return themeSettingsRepository.create(entity);
    }

    public ThemeSettings updateSetting(ThemeSettings entity) {
        Asserts.notNull(entity, "主题设置实体不能为空");
        Asserts.notNull(entity.getId(), "设置ID不能为空");
        ThemeSettings existing = themeSettingsRepository.getById(entity.getId());
        Asserts.notNull(existing, "主题设置不存在");
        return themeSettingsRepository.update(entity);
    }

    public boolean deleteSetting(UUID id) {
        ThemeSettings existing = themeSettingsRepository.getById(id);
        Asserts.notNull(existing, "主题设置不存在");
        return themeSettingsRepository.removeById(id);
    }

    public ThemeSettings getById(UUID id) {
        ThemeSettings entity = themeSettingsRepository.getById(id);
        Asserts.notNull(entity, "主题设置不存在");
        return entity;
    }

    public ThemeSettings getByKey(String settingKey) {
        ThemeSettings entity = themeSettingsRepository.getByKey(settingKey);
        Asserts.notNull(entity, "主题设置不存在, key=" + settingKey);
        return entity;
    }

    public List<ThemeSettings> listAll() {
        return themeSettingsRepository.listAll();
    }

    public List<ThemeSettings> listUserCustomizable() {
        return themeSettingsRepository.listUserCustomizable();
    }

    /**
     * 批量更新设置值
     */
    public void batchUpdate(List<ThemeSettings> settings) {
        Asserts.notNull(settings, "设置列表不能为空");
        for (ThemeSettings setting : settings) {
            Asserts.notNull(setting.getId(), "设置ID不能为空");
            ThemeSettings existing = themeSettingsRepository.getById(setting.getId());
            Asserts.notNull(existing, "主题设置不存在, id=" + setting.getId());
            existing.update(setting.getSettingValue(), setting.getDescription(), setting.getHasUserCustomizable());
            themeSettingsRepository.update(existing);
        }
        log.info("批量更新主题设置完成, count={}", settings.size());
    }
}
