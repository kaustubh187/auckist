package com.pbop.services;

import com.pbop.models.User;
import com.pbop.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
//        System.out.println("Populating User Principal");
        if(user == null){
            System.out.println("User Not found");
            throw new UsernameNotFoundException("username not found");
        }
        String authorityName = "ROLE_" + user.getRole().name();
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(authorityName)
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
