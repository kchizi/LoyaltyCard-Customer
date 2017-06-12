## LoyaltyCard Customer App

**LoyaltyCard is a loyalty card replacement system for Android. It consists of a Android application for customers, and Android application for vendors, and a backend API deployed using firebase cloud functions. The system requires a firebase account.**
<br>
<br>

Links to vendor app and cloud-functions repositories:
<br>
<br>
[LoyaltyCard Vendor App](https://github.com/fullstacknz/LoyaltyCard-Vendor)

[LoyaltyCard Cloud Functions](https://github.com/fullstacknz/cloud-functions)

<br>
<br>

### Setup Instructions

This project was created in Android Studio and requires a Firebase App to run. To create a Firebase Application you can visit the Firebase Website and sign in with your Google account.

[https://firebase.google.com/](https://firebase.google.com/)

After signing in create a new app and choose the option to 'Add another app'. Follow the instructions to connect the this Android app to your Firebase app.
<br>
<br>
LoyaltyCard will not run as intended if you do not provide your SHA-1 certificate to Firebase. This is also true for running LoyaltyCard in your development environment.

#### Required Files

For the app to run you must add the **google-services.json** file that is provided by Firebase to the **./app** subdirectory of the project.
<br>
<br>

**It is recommended that you set up cloud functions using the link provided above before continuing.**

The project also requires that a Java Class file called **CONFIG.java** is added to the path

`./app/src/main/java/card/loyalty/loyaltycardcustomer/`. 

The contents of this file needs to structured as follows

        package card.loyalty.loyaltycardcustomer;

        public class CONFIG {

            public static final String REGISTER_CLOUD_FUNCTION = ""; // url for register cloud function goes here

        }

You will need to fill in the missing string value with the url for your cloud function once you have set it up. This can be found in the Firebase console under **Functions**

<br>
The application should now be able to run in Android Studio.

<br>

### Using LoyaltyCard Customer App

After launching the app the customer will be required to sign up using either email or a Google account. The email address is not verified, but each **user** is required to have a unique email. One account will sign in to both the vendor app and the customer app.

<br>

Once signed in the app will display a QR code to identify the customer. The customer should display this to the vendor when making a purchase that relates to an offer that vendor has established. The vendor should then scan the code. This will create a loyalty card associated with that offer in the customer's cards list. If the customer already has a card for that offer it will increase the purchase count by 1.

<br>

Cards can be viewed on the MyCards screen accessible from the navigation drawer. Cards can be viewed in more detail by tapping on them once. Additional promotions can be viewed from the promotions screen.