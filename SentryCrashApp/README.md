# Test App


### Development

To start the development server:
- `yarn install`
- `npx react-native start`

To run the app:
- Open android studio and click the "run" button

### Production Build:

From the `android` directory, run:
- `./gradlew clean && ./gradlew assembleRelease`


### To reproduce a reported crash:
- Follow one of the above steps to install the app
- Click one of the three "Crash" buttons in the app menu
- A crash should be triggered and an event will appear in Sentry

### To reproduce an unreported crash:
- Follow one of the above steps to install the app
- Open Android Auto
- Navigate to the SentryCrashApp and open it
- A crash should be triggered and an event will not appear in Sentry
