package com.marian.owncloudbackend.security;


import com.marian.owncloudbackend.filters.CustomAuthenticationFilter;
import com.marian.owncloudbackend.filters.CustomAuthorisationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    @Value("${application.secret:secret}")
    private String secret;

    @Bean
    @SuppressWarnings(value = "deprecation")
    public PasswordEncoder bCryptPasswordEncoder(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();//use this if you can
        NoOpPasswordEncoder encoder1 = (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
        System.out.println(encoder.encode("password"));
        return encoder1;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();

        var customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        var customAuthorisationFilter = new CustomAuthorisationFilter();

        customAuthorisationFilter.setSecret(this.secret);
        customAuthenticationFilter.setSecret(this.secret);

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login")
                .permitAll()
                .antMatchers(HttpMethod.OPTIONS,"/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .and()
                .addFilter(customAuthenticationFilter);

        http.addFilterBefore(customAuthorisationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }
}
