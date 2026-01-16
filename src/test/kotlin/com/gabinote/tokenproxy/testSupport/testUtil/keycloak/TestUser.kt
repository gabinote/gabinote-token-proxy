package com.gabinote.tokenproxy.testSupport.testUtil.keycloak

enum class TestUser(
    val id: String,
    val password: String,
    val sub: String,
) {
    GUEST(
        id = "guest@gabinote.coom",
        password = "guest",
        sub = "86b1bc2c-0199-4d91-a6b8-29db7794ceb8",
    ),

    USER(
        id = "user@gabinote.coom",
        password = "user",
        sub = "4c1d15d0-0bef-4e7a-9ea9-b65d277863a0",
    ),

    ADMIN(
        id = "admin@gabinote.coom",
        password = "admin",
        sub = "4c0496b2-d9b0-49d2-95cc-a8aec1563d98",
    ),

    INVALID(
        id = "invalid",
        password = "invalid",
        sub = "invalid",
    );


    override fun toString(): String = id
}