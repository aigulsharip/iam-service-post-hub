package com.post_hub.iam_service.security.encrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String firstPassword = encoder.encode("password");
        String secondPassword = encoder.encode("adhsj");
        String thirdPassword = encoder.encode("fjhjdk");

        System.out.println("Hashed first " + firstPassword);
        System.out.println(secondPassword);
        System.out.println(thirdPassword);
    }
}
