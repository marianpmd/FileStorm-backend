package com.marian.owncloudbackend.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.marian.owncloudbackend.dto.SystemInfoDTO;
import com.marian.owncloudbackend.dto.UserDTO;
import com.marian.owncloudbackend.entity.DirectoryEntity;
import com.marian.owncloudbackend.entity.FileEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.exceptions.AbnormalAssignmentAmountException;
import com.marian.owncloudbackend.mapper.UserMapper;
import com.marian.owncloudbackend.repository.DirectoryRepository;
import com.marian.owncloudbackend.repository.UserRepository;
import com.marian.owncloudbackend.utils.FileStoreUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DirectoryRepository directoryRepository;

    public List<UserDTO> getAllUsers() {
        List<UserEntity> all = userRepository.findAll();
        return userMapper.entitiesToDTOs(all);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity byEmail = userRepository.findByEmail(email)
                .orElseThrow();
        //check password
        log.info("User FOUND in the db" + email);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(byEmail.getRole()));

        return new User(byEmail.getEmail(), byEmail.getPassword(), authorities);
    }

    public UserEntity getUserByEmail(String userEmail) {
        return this.userRepository.findByEmail(userEmail)
                .orElseThrow();
    }

    public UserDTO getUserDTObyEmail(String userEmail) {
        UserEntity userByEmail = getUserByEmail(userEmail);
        return userMapper.entityToDTO(userByEmail);
    }

    public UserEntity registerNewUser(String email, String password, String role) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        var newUser = new UserEntity(email, passwordEncoder.encode(password), role);

        return this.userRepository.save(newUser);
    }

    public void updateUserSpace(UserEntity userByEmail, BigInteger size) {
        BigInteger currentlyOccupied = userByEmail.getOccupiedSpace();
        userByEmail.setOccupiedSpace(currentlyOccupied.add(size));
        userRepository.save(userByEmail);
    }

    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("USER WITH ID" + userId + " NOT FOUND"));
    }

    public void deleteUser(UserEntity userEntity) {
        userRepository.delete(userEntity);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long getTotalAssignedSpace() {
        List<UserEntity> all = userRepository.findAll();
        long total = 0L;
        for (UserEntity userEntity : all) {
            total+=userEntity.getAssignedSpace().longValueExact();
        }

        return total;
    }

    public UserDTO assignToUser(Long userId, String amount, Long usableSpace) {
        long requestedAmount = FileStoreUtils.parseAmountString(amount);
        if (requestedAmount > usableSpace)
            throw new AbnormalAssignmentAmountException("Not enough sys space to assign!");


        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist " + userId));

        if (userEntity.getOccupiedSpace().longValueExact() > requestedAmount)
            throw new AbnormalAssignmentAmountException("User has already occupied more space!");
        userEntity.setAssignedSpace(BigInteger.valueOf(requestedAmount));

        return userMapper.entityToDTO(userRepository.save(userEntity));
    }

    public UserEntity recomputeUserStorage(UserEntity user) {
        List<FileEntity> files = user.getFiles();
        if (files.isEmpty()){
            user.setOccupiedSpace(BigInteger.ZERO);
            return userRepository.save(user);
        }
        BigInteger total = BigInteger.ZERO;
        for (FileEntity file : files) {
            total = total.add(file.getSize());
        }
        user.setOccupiedSpace(total);
        return userRepository.save(user);
    }

    public UserDTO deleteUserById(Long userId) throws IOException {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("User was not found!"));

        List<DirectoryEntity> byUser = directoryRepository.findByUser(userEntity);
        directoryRepository.deleteAll(byUser);

        Path defaultPath = FileStoreUtils.computePathFromRoot(userEntity.getEmail(), Collections.emptyList());

        FileUtils.forceDelete(defaultPath.toFile());

        this.deleteUser(userEntity);

        return userMapper.entityToDTO(userEntity);
    }

    public SystemInfoDTO getSystemInfo() {

        File file = new File(FileStoreUtils.getBaseDir());
        long totalSpace = file.getTotalSpace();
        long usableSpace = file.getUsableSpace();
        long totalAssignedSpace = getTotalAssignedSpace();
        usableSpace = usableSpace - totalAssignedSpace;

        return SystemInfoDTO.builder()
                .totalSpace(totalSpace)
                .usableSpace(usableSpace)
                .build();
    }
}
