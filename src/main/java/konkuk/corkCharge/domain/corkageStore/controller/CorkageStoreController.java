package konkuk.corkCharge.domain.corkageStore.controller;

import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.service.CorkageStoreService;
import konkuk.corkCharge.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/corkages")
@RequiredArgsConstructor
public class CorkageStoreController {

    private final CorkageStoreService corkageStoreService;

    @PostMapping
    public BaseResponse<Void> createCorkage(
            @RequestBody PostAddCorkageRequest request
    ) {
        corkageStoreService.createCorkage(request);
        return BaseResponse.ok(null);
    }

}