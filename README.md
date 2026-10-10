# Train_Notification_App

An app that monitors whether certain Amtrak trains have departed specific stations, and notifies you when they have.

It polls the [Amtraker API](https://github.com/piemadd/amtrak) (Amtrak, VIA Rail and Brightline) from a
foreground service, by default every 60 seconds, and posts a notification when a watched train's stop at
your station is marked departed. Departures that already happened when you press Start are skipped.

Train data from [Amtraker](https://amtraker.com).

## Run it

1. Open this folder in Android Studio and let it sync (it downloads Gradle and the Android SDK pieces).
2. Plug in your phone with USB debugging on, then press Run.
3. Allow notifications. For each train, enter its number and the station code (for example `PHL`) and tap Add train,
   then tap Start monitoring. Each train can be watched at a different station.

## Code map

- `AmtrakerClient.kt` fetches `https://api.amtraker.com/v3/trains/{number}` and parses it.
- `DepartureChecker.kt` decides whether a run has left the station (unit tested).
- `MonitorService.kt` is the polling loop and the persistent "Watching trains" notification.
- `MainActivity.kt` is the setup screen.
