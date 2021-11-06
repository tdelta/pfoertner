# Old project, Cleanup in progress!

# How to build & self-host

The APKs distributed on GitHub belong to a demo instance of this software.
To build and self-host your own version you need to setup a Firebase project
and host a Nodejs service.

## Step 0: Private clone / fork

To build your own instance of the Pfoertner service, we will need to add a few
secret files to this project in the following steps.

Hence, you should first make sure, your downstream clone/fork of this repository
is private.

## Step 1: Setting up Firebase for the PfoertnerAdmin app

This guide follows https://firebase.google.com/docs/android/setup#console

1. Create a Firebase project at https://console.firebase.google.com/
   You will need a Google account for this.

2. Open your project.

3. Register the admin app. For this, click on the Android icon.

4. Enter the following package name: `de.tu_darmstadt.epool.pfoertneradmin`

5. Enter some app nickname. E.g. `PfoertnerAdmin`

6. Download the `google-services.json` file offered to you. Place it at the
   following path in your clone of the project:

   `PfoertnerAdmin/app/`

## Step 2: Setting up Firebase for the PfoertnerPanel app

We will now repeat the above steps with minor changes to setup the PfortnerPanel
app:

1. Create a Firebase project at https://console.firebase.google.com/

2. Open your project.

3. Register the panel app. For this, click on the Android icon.

4. Enter the following package name: `de.tu_darmstadt.epool.pfoertnerpanel`

5. Enter some app nickname. E.g. `PfoertnerPanel`

6. Download the `google-services.json` file offered to you. Place it at the
   following path in your clone of the project:

   `PfoertnerPanel/app/`

## Step 3: Download Firebase Server Secrets

The backend server of Pfoertner needs a private key to access the Firebase
project. In this step, we will create it and provide it to the backend.

We will roughly follow the steps described here:
https://firebase.google.com/docs/admin/setup#initialize-sdk

1. Go to
   https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk
   and open the Pfoertner project.

2. Generate a new private key in the displayed dialog.
   Download the file and save it as
   `PfoertnerServer/firebase/firebase-secret.json`

## Step 4: Setting up the backend server

1. The backend server is implemented in node.js. Prepare a system with `node`
   and `npm` installed.

2. The backend will bind a HTTP server to the local interface on port 3000.
   You might want to setup a (reverse) proxy in Apache or nginx to add SSL
   encryption.

3. Configure the public facing URL of your backend in `./PfoertnerServer/.env`.

   E.g.
   ```
   PUBLIC_URL="https://myserver.de"
   ```

   This config example assumes a (reverse) proxy is in place which applies SSL
   encryption and binds to port 443.

   If you do not want to setup a Apache or nginx (reverse) proxy to apply
   encryption (**strongly discouraged!**), you can configure the URL like this:

   ```
   PUBLIC_URL="http://myserver.de:3000"
   ```

4. Also configure the URL in the end user app settings. That is, open the file
   `./PfoertnerCommon/src/main/java/de/tu_darmstadt/epool/pfoertner/common/Config.java`
   and change the variable `SERVER_ADDR` to the same public url as before.

   That is, the file should look something like this:

   ```java
   package de.tu_darmstadt.epool.pfoertner.common;
   
   /**
    * This class contains global settings
    */
   public class Config {
       public static final String SERVER_ADDR = "https://myserver.de/";
       public static final String PREFERENCES_NAME = "MainPrefs";
   }
   ```

5. Install the backend dependencies by running `npm install` in the
   `PfoertnerServer` directory.

6. Run the backend server using `npm start`.
   You might to wrap this command into a systemd (user) service or something
   similar so that the backend server runs at startup.

## Step 5: Build the end user apps

TODO Description

## Step 6: Deploy apps

TODO Description
