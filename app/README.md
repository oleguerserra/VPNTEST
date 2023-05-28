# Building the Android Application

This guide provides instructions for building the Android application using the provided Gradle script.

## Prerequisites

Before building the Android application, make sure you have the following prerequisites installed:

- Android Studio
- Android SDK
- Java Development Kit (JDK)

## Building the Application

To build the Android application, follow these steps:

1. Clone or download the project from the repository.

2. Open Android Studio.

3. Select "Open an existing Android Studio project" and navigate to the project directory.

4. Android Studio will import the project and synchronize the Gradle files.

5. Once the project is successfully imported, locate the `Build Variants` tab on the left side of the Android Studio window.

6. In the `Build Variants` tab, select the desired build variant (e.g., `release`).

7. Open a terminal window within Android Studio or use the built-in terminal.

8. Run the following command to build the application:

   ```
   ./gradlew assemble
   ```

   This command will compile the source code, package the application, and generate the APK file.

9. After the build process completes, navigate to the project directory and locate the `app/build/outputs/apk` folder.

10. Inside the `apk` folder, you will find the generated APK file named `VPNTEST(versionName).apk`, where `versionName` corresponds to the version specified in the Gradle script.

11. The generated APK file is ready for installation on an Android device. You can either transfer it to your device or use Android Studio to run the application on a connected device or emulator.

   - To install the APK manually, transfer the APK file to your Android device and navigate to the file using a file manager. Tap on the APK file to start the installation process.

   - To run the application using Android Studio, ensure that your Android device is connected to your computer via USB, and click on the "Run" button in the Android Studio toolbar. Select the target device or emulator, and the application will be installed and launched automatically.

12. Congratulations! You have successfully built the Android application.

## Configuration

The Gradle script provided in the project includes several configuration options. If you need to modify any of these configurations, you can edit the `build.gradle` file:

- `compileSdk`: Specifies the Android SDK version to compile against. The current value is set to `33`.

- `applicationId`: Defines the package name for the application.

- `minSdk`: Specifies the minimum Android SDK version required to run the application. The current value is set to `21`.

- `targetSdk`: Specifies the target Android SDK version for the application. The current value is set to `32`.

- `versionCode`: An integer value that represents the version code of the application.

- `versionName`: A string value that represents the version name of the application.

- `manifestPlaceholders`: Defines a set of placeholders that can be accessed from the AndroidManifest.xml file.

- `buildTypes`: Specifies different build types, such as `release`, with specific configurations like code obfuscation.

- `compileOptions`: Defines Java source compatibility options for the project.

- `buildFeatures`: Enables or disables specific build features. The current configuration enables View Binding.

## Dependencies

The project includes several dependencies that are automatically resolved by Gradle. The dependencies are listed in the `dependencies` block of the Gradle script.

Here are some notable dependencies used in the project:

- `androidx.appcompat:appcompat:1.6.1`: Provides compatibility support for newer Android features on older versions of Android.

- `com.google.android.material:material:1.9.0`: Offers a set of UI components following the Material

 Design guidelines.

- `androidx.constraintlayout:constraintlayout:2.1.4`: Provides a flexible layout manager for creating complex UI designs.

- `androidx.navigation:navigation-fragment:2.5.3` and `androidx.navigation:navigation-ui:2.5.3`: Libraries for implementing navigation and handling app navigation behavior.

- `com.squareup.okhttp3:okhttp:4.9.0`: A popular HTTP client library for making network requests.

- `com.google.code.gson:gson:2.8.9`: A library for serializing and deserializing Java objects to/from JSON.

- `junit:junit:4.13.2`: A framework for writing and running unit tests in Java.

- `androidx.test.ext:junit:1.1.5`: Provides additional JUnit functionality for testing Android applications.

- `androidx.test.espresso:espresso-core:3.5.1`: A framework for creating UI tests for Android applications.

Feel free to explore these dependencies further if you need to understand their usage or want to add additional dependencies to your project.

That's it! You now have the necessary information to build the Android application using the provided Gradle script.

## License

This project is licensed under the [GNU GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/gpl-3.0.en.html). Please review the license file for more details.

## Author

This code was written by Oleguer Serra ([oleguer@serra.cat](mailto:oleguer@serra.cat)). If you have any questions or feedback, please feel free to reach out.

