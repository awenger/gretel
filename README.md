# gretel
gretel - Gradle plugin that adds system trace events to an Android app

## Setup
```kt
plugins {
    id("de.awenger.gretel") version "0.0.3"
}
```

## How to
- Apply the gretel gradle plugin
- Capture a system trace on the device, see [here](https://developer.android.com/topic/performance/tracing/on-device)
- Launch the app
- Stop the system trace capture
- Pull the traces from the device via `adb pull /data/local/traces/ .`
- Inspect the trace in Android studio (drag the trace file onto the bar of open files or open the Profiler tab and select `+` > `Load from file...`)

## Traces are added to

- Android [Application](https://developer.android.com/reference/android/app/Application) lifecycle callbacks (onCreate, onConfigurationChanged, ...)
- Android [Activity](https://developer.android.com/reference/android/app/Activity) lifecycle callbacks (onCreate, onResume, ...)
- Android [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment) lifecycle callbacks (onCreate, onResume, ...)
- [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver)::onReceive
- Dagger [Factory](https://github.com/google/dagger/blob/c40811e71012c0838b83c3dd6b921f42332f2831/java/dagger/internal/Factory.java)::get methods, that provide dependencies
- RxJava [function](https://github.com/ReactiveX/RxJava/tree/3.x/src/main/java/io/reactivex/rxjava3/functions) interfaces
- More to come soon...

## Traces
The gretel plugin adds the traces during the app build.
The traces will be recorded via [androidx.core.os.TraceCompat](https://developer.android.com/reference/androidx/core/os/TraceCompat), by adding calls to [TraceCompat::beginSection](https://developer.android.com/reference/androidx/core/os/TraceCompat#beginSection(java.lang.String)) and [TraceCompat::endSection](https://developer.android.com/reference/androidx/core/os/TraceCompat#endSection()) in the relevant parts of the app
