package com.example.mvccrud.member;

import com.example.mvccrud.global.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(
        @RequestBody @Valid MemberCreateRequest request
    ) {
        Member member = memberService.createMember(request.getName()
            , request.getEmail(),
            request.getAge());

        ApiResponse<MemberResponse> response = ApiResponse.of(
            new MemberResponse(member));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }


    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> findMember(@PathVariable Long id) {
        Member member = memberService.findMember(id);
        return ApiResponse.of(new MemberResponse(member));
    }


    @GetMapping
    public ApiResponse<List<MemberResponse>> findMembers() {
        List<MemberResponse> members = memberService.findMembers().stream()
            .map(MemberResponse::new)
            .toList();
        return ApiResponse.of(members);
    }


    @PutMapping("/{id}")
    public ApiResponse<MemberResponse> updateMember(
        @PathVariable Long id,
        @RequestBody @Valid MemberUpdateRequest request
    ) {
        Member member = memberService.updateMember(
            id, request.getName(), request.getEmail(), request.getAge()
        );
        return ApiResponse.of(new MemberResponse(member));
    }

    @PatchMapping("/{id}")
    public ApiResponse<MemberResponse> patchMember(@PathVariable Long id
        , @RequestBody @Valid MemberPatchRequest request) {
        Member member = memberService.patchMember(
            id, request.getName(), request.getEmail(), request.getAge()
        );

        return ApiResponse.of(new MemberResponse(member));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ApiResponse<List<MemberResponse>> searchMembers(
        @ModelAttribute MemberSearchRequest request) {

        List<MemberResponse> members = memberService.searchMembers(
                request.getName(),
                request.getEmail()
            ).stream().map(MemberResponse::new)
            .toList();

        return ApiResponse.of(members);
    }




}
