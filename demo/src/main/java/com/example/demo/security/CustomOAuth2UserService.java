package com.example.demo.security;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final StudentService studentService;

    public CustomOAuth2UserService(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("🔐 [OAuth2] Loading user from provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        logger.debug("🔐 [OAuth2] Google user info - Email: {}, Name: {}", email, name);
        logger.debug("🔐 [OAuth2] User attributes: {}", oauth2User.getAttributes());

        if (email == null || email.isBlank()) {
            logger.error("❌ [OAuth2] Email not found in Google response!");
            throw new OAuth2AuthenticationException("Tài khoản Google không trả về email");
        }

        logger.debug("🔐 [OAuth2] Creating or getting OAuth student for email: {}", email);
        Student student = studentService.createOrGetOauthStudent(email, name);
        logger.debug("✅ [OAuth2] Student loaded/created: {} (ID: {})", student.getUsername(), student.getId());

        Set<GrantedAuthority> authorities = new HashSet<>();
        student.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().name())));
        logger.debug("🔐 [OAuth2] User authorities: {}", authorities);

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }
}
