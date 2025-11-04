package konkuk.corkCharge.domain.tip.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.dto.request.PostTipRequest;
import konkuk.corkCharge.domain.tip.dto.response.GetTipDetailResponse;
import konkuk.corkCharge.domain.tip.dto.response.GetTipListResponse;
import konkuk.corkCharge.domain.tip.repository.TipRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.TIP;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.TIP_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TipService {

    private final TipRepository tipRepository;
    private final S3ImageService s3ImageService;
    private final ImageRepository imageRepository;

    @Transactional
    public void createTip(PostTipRequest request, List<MultipartFile> images){

        Tip tip = Tip.builder()
                .title(request.title())
                .content(request.content())
                .tipCategory(request.tipCategory())
                .build();

        tipRepository.save(tip);

        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = s3ImageService.uploadImages(images, TIP, null);

            List<Image> imageEntities = new ArrayList<>();
            for (String url : uploadedUrls) {
                Image image = Image.builder()
                        .typeId(tip.getTipId())
                        .category(TIP)
                        .imageUrl(url)
                        .build();
                imageEntities.add(image);
            }
            imageRepository.saveAll(imageEntities);

        }
    }

    @Transactional
    public List<GetTipListResponse> getTips(){
        return tipRepository.findAll().stream()
                .map(GetTipListResponse::from)
                .toList();
    }

    @Transactional
    public GetTipDetailResponse getTipDetail(Long tipId){
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new CustomException(TIP_NOT_FOUND));

        return GetTipDetailResponse.from(tip);
    }




}
