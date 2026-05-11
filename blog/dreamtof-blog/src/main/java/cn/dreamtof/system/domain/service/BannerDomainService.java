package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.Banners;
import cn.dreamtof.system.domain.repository.BannersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerDomainService {

    private final BannersRepository bannersRepository;

    public Banners createBanner(Banners entity) {
        Asserts.notNull(entity, "横幅实体不能为空");
        return bannersRepository.create(entity);
    }

    public Banners updateBanner(Banners entity) {
        Asserts.notNull(entity, "横幅实体不能为空");
        Asserts.notNull(entity.getId(), "横幅ID不能为空");
        Banners existing = bannersRepository.getById(entity.getId());
        Asserts.notNull(existing, "横幅不存在");
        return bannersRepository.update(entity);
    }

    public boolean deleteBanner(UUID id) {
        Banners existing = bannersRepository.getById(id);
        Asserts.notNull(existing, "横幅不存在");
        existing.markDeleted();
        bannersRepository.update(existing);
        log.info("横幅软删除完成, bannerId={}", id);
        return true;
    }

    public Banners toggleEnabled(UUID id, boolean enabled) {
        Banners existing = bannersRepository.getById(id);
        Asserts.notNull(existing, "横幅不存在");
        existing.toggleEnabled(enabled);
        return bannersRepository.update(existing);
    }

    public Banners toggleCarousel(UUID id, boolean carousel) {
        Banners existing = bannersRepository.getById(id);
        Asserts.notNull(existing, "横幅不存在");
        existing.toggleCarousel(carousel);
        return bannersRepository.update(existing);
    }

    public Banners getById(UUID id) {
        Banners entity = bannersRepository.getById(id);
        Asserts.notNull(entity, "横幅不存在");
        return entity;
    }

    public List<Banners> listAll() {
        return bannersRepository.listAll();
    }

    public List<Banners> listByPosition(String position) {
        return bannersRepository.listByPosition(position);
    }

    public List<Banners> listEnabled() {
        return bannersRepository.listEnabled();
    }

    public List<Banners> listCarousel() {
        return bannersRepository.listCarousel();
    }
}
