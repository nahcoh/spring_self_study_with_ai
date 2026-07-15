package com.example.mvccrud.member;

import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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

@Tag(name = "Member API", description = "회원 등록, 조회, 수정, 삭제, 검색 API")
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "회원 등록", description = "이름, 이메일, 나이를 입력받아 새 회원을 등록합니다.")
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


    @Operation(summary = "회원 단건 조회",description = "ID로 회원 한 명을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> findMember(@PathVariable Long id) {
        Member member = memberService.findMember(id);
        return ApiResponse.of(new MemberResponse(member));
    }


    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징과 정렬 조건으로 조회합니다.")
    @GetMapping
    public ApiResponse<PageResponse<MemberResponse>> findMembers(
        @PageableDefault(size = 10, sort = "id", direction = Direction.DESC)
        Pageable pageable
    ) {

        Page<MemberResponse> members = memberService.findMembers(pageable)
            .map(MemberResponse::new);
        return ApiResponse.of(PageResponse.from(members));
    }


    @Operation(summary = "회원 전체 수정", description = "ID에 해당하는 회원의 이름, 이메일, 나이를 전체 수정합니다.")
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

    @Operation(summary = "회원 부분 수정", description = "ID에 해당하는 회원의 이름, 이메일, 나이를 부분 수정합니다.")
    @PatchMapping("/{id}")
    public ApiResponse<MemberResponse> patchMember(@PathVariable Long id
        , @RequestBody @Valid MemberPatchRequest request) {
        Member member = memberService.patchMember(
            id, request.getName(), request.getEmail(), request.getAge()
        );

        return ApiResponse.of(new MemberResponse(member));
    }

    @Operation(summary = "회원 삭제", description = "ID에 해당하는 회원을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 검색", description = "이름, 이메일 조건으로 회원을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<PageResponse<MemberResponse>> searchMembers(
        @ModelAttribute MemberSearchRequest request,
        @PageableDefault(size = 10, sort = "id", direction = Direction.DESC)
        Pageable pageable) {
        Page<MemberResponse> members = memberService.searchMembers(
            request.getName(),
            request.getEmail(),
            pageable
        ).map(MemberResponse::new);

        return ApiResponse.of(PageResponse.from(members));
    }




}
