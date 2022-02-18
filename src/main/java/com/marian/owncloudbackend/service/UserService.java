package com.marian.owncloudbackend.service;

import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.mapper.UserMapper;
import com.marian.owncloudbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public List<UserDTO> getAllUsers(){
        List<UserEntity> all = userRepository.findAll();
        return userMapper.entitiesToDTOs(all);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity byEmail = userRepository.findByEmail(email);
        //check password
        if (byEmail == null) {
            log.error("User not found in the db");
            throw new UsernameNotFoundException("User not found in the db");
        } else {
            log.info("User FOUND in the db" + email);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(byEmail.getRole()));

        return new User(byEmail.getEmail(), byEmail.getPassword(), authorities);
    }

    public UserEntity getUserByEmail(String userEmail) {
        return this.userRepository.findByEmail(userEmail);
    }

    public UserDTO registerNewUser(String email, String password) {

        var newUser = new UserEntity(email, password, "todo");

        UserEntity saved = this.userRepository.save(newUser);
        return userMapper.entityToDTO(saved);
    }
}
