package com.baseplus.modules.registros.security;

public final class ApiKeyHashGenerator {

    private ApiKeyHashGenerator() {
    }

    public static void main(String[] args) {
        if (args == null || args.length != 1 || args[0] == null || args[0].isBlank()) {
            System.err.println("Uso: java com.baseplus.modules.registros.security.ApiKeyHashGenerator <api-key>");
            System.exit(1);
        }

        System.out.println(ApiKeyHash.sha256(args[0].trim()));
    }
}
