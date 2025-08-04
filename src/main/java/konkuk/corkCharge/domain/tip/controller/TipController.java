package konkuk.corkCharge.domain.tip.controller;

import konkuk.corkCharge.domain.tip.dto.request.PostTipRequest;
import konkuk.corkCharge.domain.tip.dto.response.GetTipListResponse;
import konkuk.corkCharge.domain.tip.service.TipService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tips")
@RequiredArgsConstructor
public class TipController {
    private final TipService tipService;

    @PostMapping
    public BaseResponse<Void> createTip(
            @RequestPart(value="request") PostTipRequest request,
            @RequestPart(value="images", required = false) List<MultipartFile> images
    ){
        tipService.createTip(request,images);
        return new BaseResponse(null);
    }

    @GetMapping
    public BaseResponse<List<GetTipListResponse>> getTips(){
        return BaseResponse.ok(tipService.getTips());
    }


}
