package com.example.mvccrud.auth;


import com.example.mvccrud.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력받아 로그인 검증을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @RequestBody @Valid LoginRequest request
    ) {
        LoginResponse response = authService.login(
            request.getEmail(),
            request.getPassword()
        );

        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}
