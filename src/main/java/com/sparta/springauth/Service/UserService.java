package com.sparta.springauth.Service;

import com.sparta.springauth.Dto.SignupRequestDto;
import com.sparta.springauth.Entity.User;
import com.sparta.springauth.Entity.UserRoleEnum;
import com.sparta.springauth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ADMIN_TOKEN(사용자, 관리자 구분)
    // 현업에서는 관리자 페이지를 따로 생성하거나 승인자에 의해 결재하는 과정으로 구현됨
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // email 중복확인
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        // 사용자 ROLE 확인
        // 사용자 확인을 위해 우선 일반 사용자 권한으로 코드 실행
        UserRoleEnum role = UserRoleEnum.USER;
        // Admin이 true(관리자)라면 실행
        if (requestDto.isAdmin()) {
            // ADMIN_TOKEN과 requestDto.getAdminToken()이 다르다면
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            // 위의 if문을 통과했으므로 사용자 권한을 ADMIN(관리자)로 덮어씌움
            role = UserRoleEnum.ADMIN; 
        }

        // 일반 사용자 등록
        User user = new User(username, password, email, role);
        userRepository.save(user);
    }
}