package com.reggie.task;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javafx.animation.KeyFrame;

import java.util.Date;

public class JwtExample {
    public static void main(String[] args) {
        // Secret key used to sign the JWT
        String secretKey = "your-secret-key";

        // Generate a JWT
        String jwt = generateJwt(secretKey);
        System.out.println("生成的 JWT: " + jwt);

        // Parse and verify the JWT
        Claims claims = parseJwt(jwt, secretKey);
        if (claims != null) {
            System.out.println("token已失效，请重新登录.");
            System.out.println("用户名: " + claims.getSubject());
            System.out.println("过期时间: " + claims.getExpiration());
        } else {
            System.out.println("token.");
        }
    }

    /**
     * 该方法接受一个密钥作为参数，然后使用JWT库来创建一个JWT JSON Web Token
     * @param secretKey 密钥
     * @return
     */
    public static String generateJwt(String secretKey) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1 hour from now

        String jwt = Jwts.builder()
                .setSubject("user123") //主题
                .setIssuedAt(now) //签发时间
                .setExpiration(expiration) //过期时间
                .signWith(SignatureAlgorithm.HS256, secretKey) //签名
                .compact();

        return jwt;
    }

    /**
     * 该方法接受一个JWT字符串和一个密钥作为参数。它使用JWT库来解析传入的JWT字符串
     * @param jwt
     * @param secretKey
     * @return
     */
    public static Claims parseJwt(String jwt, String secretKey) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims;
        } catch (Exception e) {
            return null;
        }
    }
}
