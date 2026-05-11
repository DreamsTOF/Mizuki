package cn.dreamtof.system.domain.repository;

import cn.dreamtof.system.domain.model.entity.ThemeSettings;

import java.util.List;
import java.util.UUID;

public interface ThemeSettingsRepository {

    ThemeSettings create(ThemeSettings entity);

    boolean removeById(UUID id);

    ThemeSettings update(ThemeSettings entity);

    ThemeSettings getById(UUID id);

    List<ThemeSettings> listAll();

    boolean saveBatch(List<ThemeSettings> entities);

    boolean existsById(UUID id);

    ThemeSettings getByKey(String settingKey);

    List<ThemeSettings> listUserCustomizable();

    boolean existsByKey(String settingKey);
}
