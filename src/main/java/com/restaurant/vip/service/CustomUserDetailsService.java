package com.restaurant.vip.service;

import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
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
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(staff.getEmail())
                .password(staff.getPasswordHash())
                .authorities(getAuthorities(staff))
                .accountExpired(false)
                .accountLocked(staff.isAccountLocked())
                .credentialsExpired(false)
                .disabled(!staff.getActive())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Staff staff) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authorities
        authorities.add(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()));
        
        // Add specific permissions based on role
        switch (staff.getRole()) {
            case MANAGER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_STAFF"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_AUDIT_LOGS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EDIT_ALL_GUESTS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EDIT_ALL_VISITS"));
                // Fall through to include all lower-level permissions
            case SERVER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_LOG_VISITS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EDIT_OWN_VISITS"));
                // Fall through to include all lower-level permissions
            case HOST:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_GUESTS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CREATE_GUESTS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EDIT_BASIC_GUEST_INFO"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_NOTIFICATIONS"));
                break;
        }
        
        return authorities;
    }
}