package com.restaurant.vip.service;

import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Staff staff) {
        return generateToken(new HashMap<>(), staff);
    }

    public String generateToken(Map<String, Object> extraClaims, Staff staff) {
        return buildToken(extraClaims, staff, jwtExpiration);
    }

    public String generateRefreshToken(Staff staff) {
        return buildToken(new HashMap<>(), staff, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            Staff staff,
            long expiration
    ) {
        extraClaims.put("staffId", staff.getId());
        extraClaims.put("role", staff.getRole().name());
        extraClaims.put("firstName", staff.getFirstName());
        extraClaims.put("lastName", staff.getLastName());
        
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(staff.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SignatureException e) {
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT token compact of handler are invalid", e);
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long extractStaffId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("staffId", Long.class);
    }

    public StaffRole extractRole(String token) {
        Claims claims = extractAllClaims(token);
        String roleName = claims.get("role", String.class);
        return StaffRole.valueOf(roleName);
    }

    public String extractFirstName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("firstName", String.class);
    }

    public String extractLastName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("lastName", String.class);
    }
}