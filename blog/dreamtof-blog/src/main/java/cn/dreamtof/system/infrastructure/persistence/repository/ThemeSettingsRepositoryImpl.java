package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.system.application.assembler.ThemeSettingsAssembler;
import cn.dreamtof.system.domain.model.entity.ThemeSettings;
import cn.dreamtof.system.domain.repository.ThemeSettingsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.ThemeSettingsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.ThemeSettingsPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.ThemeSettingsTableDef;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ThemeSettingsRepositoryImpl extends ServiceImpl<ThemeSettingsMapper, ThemeSettingsPO> implements ThemeSettingsRepository {

    private final ThemeSettingsAssembler assembler;
    private static final ThemeSettingsTableDef T = ThemeSettingsTableDef.THEME_SETTINGS_PO;

    @Override
    public ThemeSettings create(ThemeSettings entity) {
        ThemeSettingsPO po = assembler.toPO(entity);
        if (super.save(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public ThemeSettings update(ThemeSettings entity) {
        ThemeSettingsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public ThemeSettings getById(UUID id) {
        ThemeSettingsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<ThemeSettings> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.SETTING_KEY.asc());
        List<ThemeSettingsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public boolean saveBatch(List<ThemeSettings> entities) {
        List<ThemeSettingsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public ThemeSettings getByKey(String settingKey) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.SETTING_KEY.eq(settingKey));
        qw.limit(1);
        List<ThemeSettingsPO> poList = super.list(qw);
        if (poList.isEmpty()) {
            return null;
        }
        return assembler.toEntity(poList.get(0));
    }

    @Override
    public List<ThemeSettings> listUserCustomizable() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_USER_CUSTOMIZABLE.eq(true));
        qw.orderBy(T.SETTING_KEY.asc());
        List<ThemeSettingsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public boolean existsByKey(String settingKey) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.SETTING_KEY.eq(settingKey));
        return super.count(qw) > 0;
    }
}
