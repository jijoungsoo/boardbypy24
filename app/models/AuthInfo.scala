package models;

case class AuthInfo(
    loginIdOrEmail: String
    ,password: String
    /**
     * used for enabling remember me feature
     * remember me = keep a use logged in
     */
        ,rememberMe: Boolean
    )