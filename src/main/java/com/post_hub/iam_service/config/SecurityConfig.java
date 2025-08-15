package com.post_hub.iam_service.config;

import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.IamServiceUserRole;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.security.filter.JwtRequestFilter;
import com.post_hub.iam_service.security.handler.AccessRestrictionHandler;
import com.post_hub.iam_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AccessRestrictionHandler accessRestrictionHandler;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final AntPathRequestMatcher[] NOT_SECURED_URLS = new AntPathRequestMatcher[]{
            new AntPathRequestMatcher("/auth/login", POST),
            new AntPathRequestMatcher("/auth/register", POST),
            new AntPathRequestMatcher("/auth/refresh/token", GET),
            new AntPathRequestMatcher("/auth/confirm/**", GET),
            new AntPathRequestMatcher("/email-confirmed.html", GET),
            new AntPathRequestMatcher("/email-already-confirmed.html", GET),

            new AntPathRequestMatcher("/posts/all", GET),
            new AntPathRequestMatcher("/comments/all", GET),
            new AntPathRequestMatcher("/users/all", GET),

            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/webjars/**"),
            new AntPathRequestMatcher("/actuator/**"),
            new AntPathRequestMatcher("/oauth2/**")
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(NOT_SECURED_URLS).permitAll()
                        .requestMatchers(post("/users/create")).hasAnyAuthority(adminAccessSecurityRoles())

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(accessRestrictionHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService())
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute("email");

                            User user = userRepository.findUserByEmailAndDeletedFalse(email)
                                    .orElseThrow(() -> new RuntimeException(ApiErrorMessage.USER_NOT_FOUND_AFTER_GOOGLE_LOGIN.getMessage()));

                            String jwt = jwtTokenProvider.generateToken(user);

                            response.sendRedirect("https://post-hub-project.fun/pages/oauth2-success.html?token=" + jwt);
                        }))

                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserService userService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userService);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private String[]  adminAccessSecurityRoles () {
        return new String[]{
                IamServiceUserRole.ADMIN.name(),
                IamServiceUserRole.SUPER_ADMIN.name(),
        };
    }

    private static AntPathRequestMatcher get(String pattern) {
        return new AntPathRequestMatcher(pattern, GET);
    }

    private static AntPathRequestMatcher post(String pattern) {
        return new AntPathRequestMatcher(pattern, POST);
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return request -> {
            OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            User user = userRepository.findUserByEmailAndDeletedFalse(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername(name);
                        newUser.setPassword(null);
                        newUser.setRegistrationStatus(RegistrationStatus.ACTIVE);
                        newUser.setRoles(Set.of(roleRepository.findByName(IamServiceUserRole.USER.getRole())
                                .orElseThrow(() -> new RuntimeException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()))));
                        return newUser;
                    });

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority(IamServiceUserRole.USER.getRole())),
                    oAuth2User.getAttributes(),
                    "sub"
            );
        };
    }

}
