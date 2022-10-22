package security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import security.usermgmt.WebUserService;

@Profile("default")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final boolean USE_IN_MEMORY_AUTHENTICATION = false;

    @Autowired
    private WebUserService userService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if (USE_IN_MEMORY_AUTHENTICATION) {
            auth.inMemoryAuthentication()
                    .withUser("user").password("{noop}pwd123").authorities("ROLE_USER")
                    .and().withUser("admin").password("{noop}secret123").roles("ADMIN");
        } else {
            auth.userDetailsService(userService);
        }
    }

    @Override
    public void configure(WebSecurity web) {
        web.debug(true);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/webjars/**", "/error/**", "/anonymous").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").permitAll().defaultSuccessUrl("/authenticated")
                .and().logout().permitAll().deleteCookies("JSESSIONID");
    }
}
