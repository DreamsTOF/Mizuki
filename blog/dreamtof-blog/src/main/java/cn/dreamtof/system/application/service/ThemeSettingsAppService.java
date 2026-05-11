package cn.dreamtof.system.application.service;

import cn.dreamtof.system.api.vo.ThemeSettingVO;
import cn.dreamtof.system.application.assembler.ThemeSettingsAssembler;
import cn.dreamtof.system.domain.model.entity.ThemeSettings;
import cn.dreamtof.system.domain.service.ThemeSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeSettingsAppService {

    private final ThemeSettingService themeSettingService;
    private final ThemeSettingsAssembler assembler;

    public ThemeSettingVO createSetting(String settingKey, String settingValue,
                                        String description, Boolean hasUserCustomizable) {
        ThemeSettings entity = ThemeSettings.create(settingKey, settingValue, description, hasUserCustomizable);
        ThemeSettings created = themeSettingService.createSetting(entity);
        log.info("主题设置创建完成, settingKey={}", settingKey);
        return assembler.toVO(created);
    }

    public ThemeSettingVO updateSetting(UUID id, String settingValue,
                                        String description, Boolean hasUserCustomizable) {
        ThemeSettings existing = themeSettingService.getById(id);
        existing.update(settingValue, description, hasUserCustomizable);
        ThemeSettings updated = themeSettingService.updateSetting(existing);
        log.info("主题设置更新完成, id={}", id);
        return assembler.toVO(updated);
    }

    public boolean deleteSetting(UUID id) {
        return themeSettingService.deleteSetting(id);
    }

    public ThemeSettingVO getDetail(UUID id) {
        ThemeSettings entity = themeSettingService.getById(id);
        return assembler.toVO(entity);
    }

    public ThemeSettingVO getByKey(String settingKey) {
        ThemeSettings entity = themeSettingService.getByKey(settingKey);
        return assembler.toVO(entity);
    }

    public List<ThemeSettingVO> listAll() {
        List<ThemeSettings> entities = themeSettingService.listAll();
        return assembler.toVOList(entities);
    }

    public List<ThemeSettingVO> listUserCustomizable() {
        List<ThemeSettings> entities = themeSettingService.listUserCustomizable();
        return assembler.toVOList(entities);
    }

    public void batchUpdate(List<ThemeSettingVO> settings) {
        List<ThemeSettings> entities = new ArrayList<>(settings.size());
        for (ThemeSettingVO vo : settings) {
            ThemeSettings entity = new ThemeSettings();
            entity.setId(vo.getId());
            entity.setSettingValue(vo.getSettingValue());
            entity.setDescription(vo.getDescription());
            entity.setHasUserCustomizable(vo.getHasUserCustomizable());
            entities.add(entity);
        }
        themeSettingService.batchUpdate(entities);
        log.info("批量更新主题设置完成, count={}", settings.size());
    }
}
