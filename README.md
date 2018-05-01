# OneLogin OpenId Connect Java Samples

This repo contains Java sample apps that demonstrate the various OpenId Connect flows

1. [Authorization Code Flow using Spring Security](spring-boot-app)

## What can I use these for
OpenId Connect is a great way to add user authentication to your application
where you are depending on another party to manage the user identities.

In this case OneLogin can manage the identity of your users making it
faster to get up and running.

## Single Sign On (SSO)
By implementing OpenId Connect via OneLogin you are creating a OneLogin
session which can be used to single sign on from your custom app
into other apps that your users may have access to via the OneLogin portal

## MFA
If MFA is enabled for a user in OneLogin then they will be prompted to
enter a token during the authentication. OneLogin takes care of all of this
for you, making strong authentication much easier to implement in your app.

## Requirements
In order to run any of the samples you will need to create an OpenId Connect
app in your OneLogin Admin portal. You can [read more about how to do that here](https://developers.onelogin.com/openid-connect/connect-to-onelogin).

If you don't have a OneLogin developer account [you can sign up here](https://www.onelogin.com/developer-signup).