package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.api.vo.BannerVO;
import cn.dreamtof.system.application.assembler.BannersAssembler;
import cn.dreamtof.system.domain.model.entity.Banners;
import cn.dreamtof.system.domain.service.BannerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannersAppService {

    private final BannerDomainService bannerDomainService;
    private final BannersAssembler assembler;

    public BannerVO createBanner(String title, String imageUrl, String deviceType,
                                 String position, Integer sortOrder, Boolean hasCarousel) {
        Banners entity = Banners.create(title, imageUrl, deviceType, position, sortOrder, hasCarousel);
        Banners created = bannerDomainService.createBanner(entity);
        log.info("横幅创建完成, bannerId={}, title={}", created.getId(), title);
        return assembler.toVO(created);
    }

    public BannerVO updateBanner(UUID id, String title, String imageUrl, String deviceType,
                                 String position, Integer sortOrder) {
        Banners existing = bannerDomainService.getById(id);
        existing.update(title, imageUrl, deviceType, position, sortOrder);
        Banners updated = bannerDomainService.updateBanner(existing);
        log.info("横幅更新完成, bannerId={}", id);
        return assembler.toVO(updated);
    }

    public boolean deleteBanner(UUID id) {
        return bannerDomainService.deleteBanner(id);
    }

    public BannerVO toggleEnabled(UUID id, boolean enabled) {
        Banners updated = bannerDomainService.toggleEnabled(id, enabled);
        return assembler.toVO(updated);
    }

    public BannerVO toggleCarousel(UUID id, boolean carousel) {
        Banners updated = bannerDomainService.toggleCarousel(id, carousel);
        return assembler.toVO(updated);
    }

    public BannerVO getDetail(UUID id) {
        Banners entity = bannerDomainService.getById(id);
        return assembler.toVO(entity);
    }

    public List<BannerVO> listAll() {
        List<Banners> entities = bannerDomainService.listAll();
        return assembler.toVOList(entities);
    }

    public List<BannerVO> listByPosition(String position) {
        List<Banners> entities = bannerDomainService.listByPosition(position);
        return assembler.toVOList(entities);
    }

    public List<BannerVO> listEnabled() {
        List<Banners> entities = bannerDomainService.listEnabled();
        return assembler.toVOList(entities);
    }

    public List<BannerVO> listCarousel() {
        List<Banners> entities = bannerDomainService.listCarousel();
        return assembler.toVOList(entities);
    }

    public PageResult<BannerVO> pageBanners(PageReq pageReq) {
        // Banners 数量通常较少，提供全量分页
        List<Banners> all = bannerDomainService.listAll();
        List<BannerVO> voList = assembler.toVOList(all);
        return PageResult.of(voList, voList.size(), 1, 1, voList.size());
    }
}
