package com.spider.common.utils;

import com.spider.common.config.JwtConfiguration;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


@Component
@Slf4j
public class JwtTokenUtil implements Serializable {

    @Serial
    private static final long serialVersionUID = -2550185165626007488L;

    @Autowired
    private JwtConfiguration jwtConfiguration;

    public JwtConfiguration getJwtConfiguration() {
        return jwtConfiguration;
    }

    private boolean validateToken(DefaultClaims body) {
        if (body == null || body.get("uuid") == null || body.get("exp") == null) {
            return false;
        }
        String exp = body.get("exp").toString();
        return (Long.parseLong(exp) <= System.currentTimeMillis() && (Long.parseLong(exp) != System.currentTimeMillis()));
    }

    DefaultClaims fetchDefaultClaims(String token) {
        Key key = getKey();
        return (DefaultClaims) Jwts.parserBuilder().setSigningKey(key).build().parse(token).getBody();
    }
    private Key getKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(jwtConfiguration.getSecret()),
                SignatureAlgorithm.HS256.getJcaName());
    }

    public boolean validateToken(String token) {
        try {
            return validateToken(fetchDefaultClaims(token));
        } catch (Exception e) {
            return false;
        }
    }
    public String decodeUUID(String token) throws ExpiredJwtException {
        try {
            DefaultClaims body = fetchDefaultClaims(token);
            return (String) body.get("uuid");
        } catch (Exception e) {
            return null;
        }
    }
    public String encode(String uuid, String tokenType) {
        if ("LOGIN".equals(tokenType)) {
            return doGenerateToken(uuid, tokenType, jwtConfiguration.getLoginTTL());
        }else {
            throw new RuntimeException("Token Type is not valid");
        }
    }
    private String doGenerateToken(String uuid, String subject, long ttl) {
        log.info(jwtConfiguration.getSecret());
        Key key = getKey();
        Instant now = Instant.now();
        String jwtToken = Jwts.builder()
                .claim("uuid", uuid)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttl)))
                .signWith(key)
                .compact();
        return jwtConfiguration.getPrefix() + " " + jwtToken;
    }
    /*




    public String encode(String email, String tokenType) {
        if ("LOGIN".equals(tokenType)) {
            return doGenerateToken(email, tokenType, jwtConfiguration.getLoginTTL());
        } else if ("LOGIN_OTP".equals(tokenType)) {
            return doGenerateToken(email, tokenType, jwtConfiguration.getOtpTTL());
        } else {
            throw new RuntimeException("Token Type is not valid");
        }
    }

    private String doGenerateToken(String email, String subject, long ttl) {
        log.info(jwtConfiguration.getSecret());
        Key key = getKey();
        Instant now = Instant.now();
        String jwtToken = Jwts.builder()
                .claim("email", email)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttl)))
                .signWith(key)
                .compact();
        return jwtConfiguration.getPrefix() + " " + jwtToken;
    }
*/
	/*
	private Key getSignInKey() {
//		byte[] keyBytes = Decoders.BASE64.decode(secret);
//		byte[] keyBytes = Base64.getDecoder().decode(secret);
//		return Keys.hmacShaKeyFor(keyBytes);

		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);

	}




	public UserSessionDTO decode(String token) throws ExpiredJwtException {
		Key key = new SecretKeySpec(Base64.getDecoder().decode(secret),
				SignatureAlgorithm.HS256.getJcaName());
		try{
			DefaultClaims body = (DefaultClaims) Jwts.parserBuilder().setSigningKey(key).build().parse(token).getBody();
			boolean validated = validateToken(body);
			if(!validated){
				throw new ExpiredJwtException(null,null,JWT_TOKEN_INVALID,null);
			}
			UserSessionDTO userSessionDTO = new UserSessionDTO();
			userSessionDTO.setEmail((String)body.get("email"));
			userSessionDTO.setTokenType((String)body.get("sub"));
			return userSessionDTO;
		}catch (Exception e){
			throw new ExpiredJwtException(null,null,JWT_EXPIRED,null);
		}
	}



	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getIssuedAtDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}


	private Claims getAllClaimsFromToken(String token){
		return Jwts
				.parserBuilder()
				.setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Boolean ignoreTokenExpiration(String token) {
		// here you specify tokens, for that the expiration is ignored
		return false;
	}



	private String doGenerateToken(String email,String subject,long ttl) {
		log.info(secret);
		Key key = new SecretKeySpec(Base64.getDecoder().decode(secret),
				SignatureAlgorithm.HS256.getJcaName());
		Instant now = Instant.now();
		String jwtToken = Jwts.builder()
				.claim("email", email)
				.setSubject(subject)
				.setId(UUID.randomUUID().toString())
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plus(ttl, ChronoUnit.SECONDS)))
				.signWith(key)
				.compact();
		return prefix +" "+jwtToken;
	}

	public Boolean canTokenBeRefreshed(String token) {
		return (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}*/
}

