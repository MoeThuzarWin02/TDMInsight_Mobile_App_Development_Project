# TDM Insight

## Vancomycin Therapeutic Drug Monitoring Mobile Application

TDM Insight is a native Android academic application developed for **Vancomycin Therapeutic Drug Monitoring (TDM)**. The application provides a guided workflow for entering patient and medication information, selecting a sampling method, performing pharmacokinetic calculations, and viewing estimated AUC24 and related parameters.

> **Disclaimer:** This application is an academic prototype developed for educational purposes only. It is not clinically validated and must not be used for actual prescribing, diagnosis, or treatment decisions.

---

## Project Title

**TDM Insight: Vancomycin Therapeutic Drug Monitoring Mobile Application**

---

## Course Information

* **Course:** Mobile App Development
* **Programme:** Bachelor of Data Science
* **University:** Albukhary International University (AIU)
* **School:** School of Computing & Informatics
* **Development Platform:** Android Studio
* **Application Type:** Native Android Application
* **Domain:** Healthcare / Therapeutic Drug Monitoring
* **Academic Year:** 2026

---

## Group Members

Hnin Shwe Yee Htun : AIU24102179 : UI/UX & Frontend Developer
Moe Thuzar Win     : AIU24102185 : Pharmacokinetic & Calculation Developer
Khin Yadanar Phyo   : AIU24102203 : Application Integration, Testing & Documentation

## Case Study

### Problem Overview

Vancomycin is an antibiotic that requires careful therapeutic drug monitoring because appropriate drug exposure is important for achieving treatment effectiveness while reducing the risk of toxicity.

Performing TDM calculations manually requires several patient, medication, and drug concentration inputs. These include patient characteristics, serum creatinine, dosage information, infusion duration, drug concentrations, and sampling times.

This project aims to provide a structured mobile application that simplifies the educational demonstration of Vancomycin TDM calculations through a guided and user-friendly interface.

### Implemented Solution

TDM Insight provides three different TDM workflows:

* **Pre-dose:** Uses a pre-dose/trough concentration.
* **Post-dose:** Uses a post-dose concentration and sampling time.
* **Pre + Post:** Uses both pre-dose and post-dose concentrations for a two-point estimation.

The application validates the entered information and performs pharmacokinetic calculations before displaying the estimated results.

The implemented solution focuses on:

* Structured data entry
* Sampling method selection
* Input validation
* Pharmacokinetic calculations
* AUC24 estimation
* Explainable calculation results
* Offline operation
* Simple and user-friendly interface

---

## Key Implemented Features

### 1. Multiple TDM Workflows

The application supports:

* Pre-dose workflow
* Post-dose workflow
* Pre + Post workflow

### 2. Patient Information

Users can enter:

* Patient/Case Name
* Sex
* Age
* Body Weight
* Serum Creatinine

### 3. Medication Information

Users can enter:

* Dose
* Dosing Interval
* Infusion Duration
* Pre-dose Concentration
* Post-dose Concentration
* Post-sample Time

### 4. Input Validation

The application validates user inputs before performing calculations, including:

* Required fields
* Patient information
* Weight and age
* Serum creatinine
* Dose and dosing interval
* Infusion duration
* Drug concentrations
* Sampling time
* Calculation conditions

### 5. Pharmacokinetic Calculations

The application estimates:

* Creatinine Clearance (CrCl)
* Elimination Rate Constant (Ke)
* Elimination Half-life
* Volume of Distribution (Vd)
* Clearance
* Estimated Peak Concentration
* AUC24

### 6. Calculation Explanation

The results screen provides intermediate pharmacokinetic values and an expandable calculation explanation so users can understand how the final results are obtained.

### 7. Offline Application

The current application operates locally on the Android device and does not require:

* Internet connection
* Backend server
* Cloud database
* User account
* External API

### 8. Academic Safety Disclaimer

The application clearly indicates that the results are for academic purposes and should not be used for actual clinical decisions.

---

## Technology Stack

| Technology        | Purpose                         |
| ----------------- | ------------------------------- |
| Kotlin            | Main programming language       |
| Android Studio    | Development environment         |
| Jetpack Compose   | User interface development      |
| Material 3        | UI components and design        |
| Android SDK       | Android application development |
| AndroidX          | Android libraries               |
| Gradle Kotlin DSL | Build and dependency management |
| Git               | Version control                 |
| GitHub            | Repository and collaboration    |

---

## Application Architecture

The application follows a lightweight architecture that separates the user interface, navigation, data models, validation, and calculation logic.

```text
TDM Insight
│
├── MainActivity
│       │
│       ▼
│   TDM Theme
│       │
│       ▼
│   App Navigation
│       │
│       ├── Home Screen
│       │
│       ├── Method Selection
│       │
│       ├── Input Screen
│       │       │
│       │       ▼
│       │   Input Validation
│       │       │
│       │       ▼
│       │   TDM Calculation Engine
│       │
│       ▼
│   Results Screen
│       │
│       ├── AUC24
│       ├── CrCl
│       ├── Ke
│       ├── Half-life
│       ├── Vd
│       ├── Clearance
│       ├── Estimated Peak
│       └── Calculation Explanation
```

### Main Components

#### `MainActivity.kt`

Responsible for launching the Android application and setting up the Jetpack Compose interface.

#### `AppNavigation.kt`

Responsible for:

* Screen navigation
* Managing the application workflow
* Maintaining input state
* Triggering validation
* Triggering calculations

#### `TdmComponents.kt`

Contains the main:

* Data models
* TDM sampling methods
* Calculation engine
* Validation logic
* UI components
* Results display
* Calculation explanation

#### `TdmEngine`

Responsible for performing the main pharmacokinetic calculations, including:

* CrCl
* Ke
* Half-life
* Vd
* Clearance
* AUC24
* Estimated peak concentration

---

## Installation Guide

### Requirements

Before running the project, install:

* Android Studio
* Android SDK
* Android SDK Platform 35
* JDK 17
* Android Emulator or Android device

### Step 1: Clone the Repository

```bash
git clone <GITHUB_REPOSITORY_URL>
```

### Step 2: Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Select the cloned `TDMInsight` project folder.
4. Wait for Gradle synchronization to finish.

### Step 3: Configure the Android SDK

Make sure Android SDK Platform 35 is installed.

Go to:

```text
Tools → SDK Manager → Android SDK
```

Install the required SDK platform if it is not already installed.

### Step 4: Select a Device

You can either:

* Start an Android Emulator, or
* Connect a physical Android device with USB debugging enabled.

### Step 5: Run the Application

Click:

```text
Run ▶
```

The application will be built and installed on the selected device.

---

## How to Build the Project

### Build Using Android Studio

In Android Studio, select:

```text
Build → Make Project
```

To generate an APK:

```text
Build → Generate App Bundles or APKs → Generate APKs
```

The generated APK will normally be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Build Using Gradle

For Windows:

```bash
gradlew.bat assembleDebug
```

For macOS/Linux:

```bash
./gradlew assembleDebug
```

The generated APK can be found at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## APK Download

The latest APK can be downloaded here:

**[Download TDM Insight APK](APK/TDMInsight.apk)**

> If the APK is not included in the repository yet, generate the APK using the build instructions above and place it inside the `APK` folder.

---

## Screenshots

### Home Screen

<img width="402" height="826" alt="image" src="https://github.com/user-attachments/assets/41a90c47-0239-4cd7-b11b-c9e143198428" />


### TDM Method Selection

![TDM Method Selection](screenshots/method_screen.png)

### Assessment Input

![Assessment Input](screenshots/input_screen.png)

### Results Screen

![Results Screen](screenshots/results_screen.png)

---

## GitHub Repository Structure

```text
TDMInsight/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── example/
│   │       │           └── tdminsight/
│   │       │               ├── MainActivity.kt
│   │       │               ├── AppNavigation.kt
│   │       │               └── TdmComponents.kt
│   │       │
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   └── values/
│   │       │
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── APK/
│   └── TDMInsight.apk
│
├── screenshots/
│   ├── home_screen.png
│   ├── method_screen.png
│   ├── input_screen.png
│   └── results_screen.png
│
├── gradle/
│   └── wrapper/
│
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

---

## Acknowledgements

We would like to express our appreciation to:

* **Albukhary International University (AIU)** for providing the academic environment and resources for this project.
* **School of Computing & Informatics** for the course guidance and project requirements.
* Our course lecturer for the guidance and feedback provided throughout the Mobile App Development project.
* The Android, Kotlin, Jetpack Compose, and Material 3 communities for providing the technologies and documentation used to develop this application.

---

## References

* Android Developers Documentation
* Kotlin Documentation
* Jetpack Compose Documentation
* Material Design 3 Documentation
* Cockcroft-Gault equation for creatinine clearance estimation
* Vancomycin Therapeutic Drug Monitoring guidelines and academic references

---

## Project Status

**Status:** Completed Academic Prototype

**Version:** 1.0

### Current Capabilities

* Native Android application
* Jetpack Compose user interface
* Three TDM workflows
* Patient and medication data entry
* Input validation
* Pharmacokinetic calculation engine
* AUC24 estimation
* Calculation explanation
* Offline operation
* Academic safety disclaimer

### Future Improvements

Possible future improvements include:

* Local SQLite/Room database
* Assessment history
* Patient case management
* PDF report generation
* Exporting calculation results
* Cloud synchronization
* User authentication
* Additional therapeutic drug monitoring workflows

---

## Disclaimer

**TDM Insight is an academic software prototype developed for educational purposes.**

The results generated by this application are estimates based on the mathematical models implemented in the application. They are not clinically validated and must not be used independently for prescribing, diagnosis, treatment, or patient-care decisions.

All patient examples used with this application should be fictional or appropriately anonymized.
