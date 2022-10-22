package security.fragments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.UnsupportedEncodingException;

@SpringJUnitWebConfig
public class BasicLoginTest {

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class WebConfig implements WebMvcConfigurer {

        @Bean
        SampleController sampleController() {
            return new SampleController();
        }

    }

    // tag::sec-config[]
    @Configuration
    @EnableWebSecurity // nicht notwendig bei Spring-Boot
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/js/**", "/css/**").permitAll()
                    .antMatchers("/admin/**")
                        .hasAuthority("ROLE_ADMIN") // alternativ: hasRole("ADMIN")
                    .anyRequest().authenticated()
                    .and().httpBasic(); // basic authentication
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            auth.inMemoryAuthentication()
                    .withUser("hugo")
                    .password(encoder.encode("pwd123"))
                    .authorities("ROLE_USER") // alternativ: roles("USER")
            .and().withUser("fritz").password(encoder.encode("secret123")).roles("USER", "ADMIN");
        }
    }
    // end::sec-config[]

    @RestController
    static class SampleController {

        @GetMapping("/greeting")
        public String greeting() {
            return "Hello World!";
        }

        @GetMapping("/admin/greeting")
        public String greetAdmnis() {
            return "Admin World!";
        }

        // tag::sec-method[]
        @GetMapping("/protected")
        @PreAuthorize("hasRole('ADMIN')")  // alternativ: hasAuthority('ROLE_ADMIN')
        public String protectedGreeting() {
            return "Protected World!";
        }
        // end::sec-method[]
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setup(WebApplicationContext wac) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/greeting")
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/greeting")
                .header("Authorization", basicAuthenticationHeader("hugo", "pwd123"))
                .accept(MediaType.TEXT_PLAIN))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Hello World!"));
    }

    @Test
    void testInvalidCredentials() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/greeting")
                .header("Authorization", basicAuthenticationHeader("hugo", "pwd124"))
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testAdminNotPermitted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/greeting")
                .header("Authorization", basicAuthenticationHeader("hugo", "pwd123"))
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testProtecedNotPermitted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/protected")
                .header("Authorization", basicAuthenticationHeader("hugo", "pwd123"))
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testAdminGreeting() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/greeting")
                .header("Authorization", basicAuthenticationHeader("fritz", "secret123"))
                .accept(MediaType.TEXT_PLAIN))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Admin World!"));
    }

    @Test
    void testProtectedGreeting() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/protected")
                .header("Authorization", basicAuthenticationHeader("fritz", "secret123"))
                .accept(MediaType.TEXT_PLAIN))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Protected World!"));
    }

    private String basicAuthenticationHeader(String username, String password) throws UnsupportedEncodingException {
        String authentication = username + ":" + password;
        String header = "Basic " + Base64Utils.encodeToString(authentication.getBytes("UTF-8"));
        return header;
    }
}
