# Test App


### Development

To start the development server:
- `yarn install`
- `npx react-native start`

To run the app:
- Open the project in android studio and click the "run" button
- Alternatively, you can run `npx react-native run-android --variant ChirpDebug` in a terminal

### Production Build:

From the `android` directory, run:
- `./gradlew clean && ./gradlew assembleRelease`
- Install by running `adb install app/build/outputs/apk/chirp/release/app-chirp-release.apk`


### To reproduce a reported crash:
- Follow one of the above steps to install the app
- Click one of the three "Crash" buttons in the app menu
- A crash should be triggered and an event will appear in Sentry

### To reproduce an unreported crash:   
- Follow one of the above steps to install the app
- Open Android Auto
- Make sure you are in developer mode
- Navigate to the SentryCrashApp and open it
- A crash should be triggered and an event will not appear in Sentry
