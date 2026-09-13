# TDM Insight

A native Android academic prototype for Vancomycin Therapeutic Drug Monitoring (TDM), built with Kotlin and Jetpack Compose / Material 3.

## Features

- Three dynamic workflows: **Pre-dose**, **Post-dose**, and **Pre + Post**.
- Method-dependent input forms rather than showing every possible field.
- Required-field, numeric range, unit, timing, and mathematical-safety validation.
- Calculation engine isolated in `TdmEngine`, separate from composables.
- Review screen before calculation.
- Intermediate pharmacokinetic parameters: Ke, half-life, Vd, clearance, estimated peak, and AUC24 estimate.
- Transparent educational one-compartment intermittent-infusion equations with explicit timing and concentration validation.
- Expandable calculation explanation.
- Offline operation; no authentication, backend, analytics, or network calls.
- Academic/clinical disclaimer shown in the app.

## Open in Android Studio

1. Open Android Studio (Ladybug or newer recommended).
2. Select **Open** and choose the `TDMInsight` folder.
3. Allow Gradle sync to finish. The project uses Android Gradle Plugin 8.6.1, Kotlin 2.0.21, compile SDK 35, and min SDK 26.
4. Start an Android emulator or connect a test device.
5. Press **Run**.

If Gradle reports `SDK location not found`, open **Tools → SDK Manager** and install Android SDK Platform 35 plus Android SDK Build-Tools. Android Studio normally creates the SDK path automatically. This sandbox did not contain an Android SDK, so the source project could not produce an APK here; the project itself is configured for Android Studio.

The project includes `gradle.properties` with AndroidX enabled and a 2 GB Gradle heap. If a low-memory computer still reports Java heap errors, close other Android Studio projects and increase the IDE Gradle VM options to `-Xmx2048m` under **Settings → Build, Execution, Deployment → Build Tools → Gradle**.

## Source layout

- `app/src/main/java/com/example/tdminsight/MainActivity.kt` contains the Compose UI, state flow, validation, result model, and the isolated `TdmEngine`.
- `app/src/main/AndroidManifest.xml` defines the launcher activity.
- `app/build.gradle.kts` defines Android/Compose dependencies.

## Important clinical note

This is an educational software prototype based on the case-study requirements. It is not clinically validated, must not be used for prescribing or treatment decisions, and uses an explicitly documented population-model demonstration. The pre-dose workflow uses a 0.7 L/kg population Vd assumption; post-dose workflows use a one-compartment intermittent-infusion back-extrapolation; and the population Ke estimate is not a validated clinical renal model. Replace or verify all equations, assumptions, targets, and reference values with lecturer-approved authoritative clinical sources before any clinical or assessed deployment.
