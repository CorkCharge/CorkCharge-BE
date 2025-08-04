package konkuk.corkCharge.domain.tip.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.tip.dto.request.PostTipRequest;
import konkuk.corkCharge.domain.tip.dto.response.GetTipListResponse;
import konkuk.corkCharge.domain.tip.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.TIP;

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

        if(images !=null && !images.isEmpty()){
            List<String> uploadedUrls = s3ImageService.uploadImages(images, TIP, null);

            List<Image> imageEntities = new ArrayList<>();
            for(String url : uploadedUrls){
                Image image = Image.builder()
                        .tip(tip)
                        .imageUrl(url)
                        .category(TIP)
                        .build();
                imageEntities.add(image);
            }
            imageRepository.saveAll(imageEntities);
            tip.setImages(imageEntities);
        }
    }

    @Transactional
    public List<GetTipListResponse> getTips(){
        List<Tip> tips = tipRepository.findAll();
        return tips.stream()
                .map(GetTipListResponse::from)
                .collect(Collectors.toList());
    }


}
