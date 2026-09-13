# TDM Insight -- Case Study Analysis

## 1. Case Study Overview

**Course:** CDE2313 -- Mobile Application Development\
**Project:** TDM Insight\
**Application Type:** Native Android Therapeutic Drug Monitoring
Calculator\
**Development Platform:** Android Studio\
**Language:** Kotlin\
**UI:** Jetpack Compose / XML\
**Design:** Material 3

TDM Insight is a native Android application designed to bring patient
information, medication information, sampling data, validation,
pharmacokinetic calculations, and calculation explanations together in a
structured mobile experience. The case study focuses on Vancomycin
Therapeutic Drug Monitoring (TDM).

> **Important:** TDM Insight is an academic software prototype for
> educational and software-development purposes. It must not be
> presented as a clinically validated prescribing, diagnostic, or
> autonomous treatment-decision system. All demonstration cases must be
> fictional.

------------------------------------------------------------------------

## 2. Background of the Problem Domain

A hospital pharmacy department performs Therapeutic Drug Monitoring
(TDM) calculations for selected medicines. These calculations may
require information such as:

-   Patient information
-   Medication dose
-   Dosing interval
-   Drug concentration
-   Sampling time
-   Laboratory information

The proposed TDM Insight application brings these inputs and
calculations together into a structured native Android experience.

The concept is inspired by existing TDM calculators, including **myTDM
Calculator** and the **Malaysian Pharmacy Information System (PhIS) TDM
Calculator** documentation.

------------------------------------------------------------------------

## 3. Current Challenges

Based on the case study, the main challenges are:

1.  TDM calculations require multiple types of patient, medication,
    concentration, and sampling information.
2.  Different calculation workflows require different input fields.
3.  The application must perform more than simple empty-field checking;
    it must validate numeric values, units, ranges, and relationships
    between fields.
4.  Calculation logic can involve mathematical errors such as division
    by zero and invalid logarithmic operations.
5.  Pharmacokinetic calculations are non-trivial and should be separated
    from the user interface.
6.  Users should be able to understand how the final result was produced
    rather than seeing only a final number.
7.  Clinical equations, units, assumptions, and reference values must
    come from lecturer-approved authoritative sources rather than being
    invented by the developer.

------------------------------------------------------------------------

## 4. Target Users / Stakeholders

The case study does not provide a separate explicit target-user list.
From the stated problem domain, the application is intended to support
the **hospital pharmacy TDM calculation workflow**.

For the academic project, the application is demonstrated using
**fictional cases** only.

------------------------------------------------------------------------

## 5. Project Scope

The main objective is to build a **native Android TDM Calculator** with
a reliable calculation engine at its core.

The application should provide a clear process to:

1.  Collect required inputs.
2.  Validate the entered information.
3.  Select the appropriate Vancomycin calculation workflow.
4.  Run the calculation through a dedicated calculation engine.
5.  Display intermediate and final pharmacokinetic results.
6.  Explain how the main results were obtained.

### In Scope

-   Native Android development using Kotlin and Android Studio.
-   Jetpack Compose and/or XML.
-   Material 3 user interface.
-   Vancomycin TDM calculation workflows.
-   Dynamic input forms.
-   Input and cross-field validation.
-   Dedicated calculation engine.
-   Intermediate and final pharmacokinetic results.
-   Calculation explanation.

### Out of Scope

The following are not required for the core project:

-   User authentication.
-   Cloud backend or online services.
-   Analytics dashboards.
-   Complex patient-management systems.
-   Multiple medication modules.
-   Large educational-content modules.

------------------------------------------------------------------------

## 6. Functional Requirements

### FR1 -- Open the Application

The system shall allow the user to open TDM Insight and begin a
calculation.

### FR2 -- Create a Fictional Case

The user shall be able to enter information for a fictional TDM case.

### FR3 -- Enter Patient Parameters

The system shall allow the user to enter the required patient parameters
for the selected workflow.

### FR4 -- Select Medication

The application shall support the Vancomycin TDM workflow specified by
the case study.

### FR5 -- Select Sampling Method

The user shall be able to select one of the following workflows:

-   Vancomycin Pre
-   Vancomycin Post
-   Vancomycin Pre + Post

### FR6 -- Dynamic Input Forms

The application shall display input fields based on the selected
calculation workflow.

The application should not display every possible input field on a
single screen.

### FR7 -- Input Validation

The system shall validate:

-   Required fields.
-   Numeric values.
-   Units.
-   Specified value ranges.
-   Missing values required by the selected workflow.

### FR8 -- Cross-Field Validation

The system shall validate relationships between related inputs,
including logical timing relationships.

### FR9 -- Mathematical Error Protection

The calculation process shall protect against mathematical errors,
including:

-   Division by zero.
-   Invalid logarithmic operations.

### FR10 -- Review Calculation Inputs

The user shall be able to review the calculation inputs before running
the TDM calculation.

### FR11 -- Perform TDM Calculation

The application shall send structured input data to the calculation
engine and execute the appropriate calculation pathway.

### FR12 -- Display Intermediate Results

The application shall display relevant intermediate pharmacokinetic
results.

### FR13 -- Display Final Results

The application shall display the final result produced by the selected
calculation workflow.

### FR14 -- Explain Calculation

The application shall provide a calculation explanation showing:

``` text
Input Values
      ↓
Intermediate Values
      ↓
Pharmacokinetic Parameters
      ↓
Final Result
```

------------------------------------------------------------------------

## 7. Non-Functional Requirements

### NFR1 -- Usability

The application should provide a clean and intuitive Material 3
interface.

### NFR2 -- Reliability

The calculation engine should reliably process structured input data and
return structured results.

### NFR3 -- Maintainability

Calculation logic shall be separated from UI components rather than
being embedded directly inside Composable or UI functions.

### NFR4 -- Accuracy

Clinical equations, units, assumptions, and reference values shall be
based on lecturer-approved authoritative sources.

### NFR5 -- Validation

The system shall provide meaningful validation and clearly distinguish
between errors and information that only requires review.

### NFR6 -- Understandability

Results should be explainable so that users can follow the main
calculation steps.

### NFR7 -- Platform Compliance

The application shall be developed as a native Android application using
Kotlin and Android Studio.

------------------------------------------------------------------------

## 8. Business Rules

### BR1 -- Vancomycin Workflows

The core application shall focus on three Vancomycin workflows:

1.  **Pre:** Based on a pre-dose concentration.
2.  **Post:** Based on a post-dose concentration and its sampling
    information.
3.  **Pre + Post:** Uses both pre-dose and post-dose concentrations with
    relevant timing information.

### BR2 -- Workflow-Dependent Inputs

The selected workflow determines which input fields are required and
displayed.

### BR3 -- Fictional Demonstration Cases

All demonstration cases must be fictional.

### BR4 -- Clinical Formula Sources

Clinical equations, units, assumptions, and reference values must be
supported by appropriate authoritative sources.

### BR5 -- No Invented Clinical Formulas

Students must not invent clinical formulas.

### BR6 -- Calculation Logic Separation

Calculation logic must be implemented in a dedicated calculation engine
and separated from the UI.

### BR7 -- Result Explanation

The system should not display only the final number. Key input values
and intermediate calculations should be available through the
calculation explanation.



------------------------------------------------------------------------

## 9. Expected Application Features

The expected core application features are:

-   Home / welcome screen.
-   Assessment setup.
-   Sampling method selection.
-   Patient information input.
-   Medication and sampling data input.
-   Dynamic forms based on the selected workflow.
-   Input validation.
-   Cross-field validation.
-   Vancomycin calculation engine.
-   Intermediate pharmacokinetic results.
-   Final calculation results.
-   Calculation explanation.
-   Material 3 user interface.

### Suggested User Flow

``` text
Open TDM Insight
       ↓
Create Fictional Case
       ↓
Select Pre / Post / Pre + Post
       ↓
Enter Required Values
       ↓
Validate Information
       ↓
Review Calculation Inputs
       ↓
Run TDM Calculation
       ↓
Display Intermediate Results
       ↓
Display Final Results
       ↓
Open Calculation Explanation
```

------------------------------------------------------------------------

## 10. Calculation Engine

The calculation engine is the main technical component of the
application.

It should:

1.  Receive structured input data.
2.  Validate the input.
3.  Select the appropriate calculation pathway.
4.  Perform the required pharmacokinetic calculations.
5.  Return structured results to the UI.

### Architecture

``` text
UI
 ↓
Input State
 ↓
Validation
 ↓
TDM Calculation Engine
 ↓
Result Model
 ↓
Results UI
```

The calculation logic should **not** be embedded directly inside
Composable or UI functions.

------------------------------------------------------------------------

## 11. Pharmacokinetic Outputs

Depending on the selected workflow and lecturer-approved calculation
specification, the application may calculate:

-   Elimination rate constant (Ke)
-   Elimination half-life
-   Volume of distribution (Vd)
-   Clearance
-   Concentration-related values
-   AUC-related values
-   Other parameters required by the selected TDM method

> The exact clinical equations, units, assumptions, and reference values
> must be based on lecturer-approved authoritative sources.

------------------------------------------------------------------------

## 12. Validation Requirements

Validation shall go beyond checking whether a field is empty.

The application should perform:

  -----------------------------------------------------------------------
  Validation Type                     Purpose
  ----------------------------------- -----------------------------------
  Required-field validation           Ensures required information is
                                      entered

  Numeric validation                  Ensures numeric fields contain
                                      valid numeric values

  Unit validation                     Ensures values use the expected
                                      units

  Range validation                    Checks values against specified
                                      ranges

  Cross-field validation              Checks logical relationships
                                      between fields

  Workflow validation                 Detects values missing for the
                                      selected method

  Mathematical validation             Prevents invalid calculations such
                                      as division by zero

  Logarithmic validation              Prevents invalid logarithmic
                                      operations
  -----------------------------------------------------------------------

The application should provide clear messages that distinguish an actual
error from information that simply requires review.

------------------------------------------------------------------------

## 13. Explainable Results

The result screen should provide more than a final number.

Users should be able to open a calculation explanation containing:

1.  Key input values.
2.  Intermediate calculations.
3.  Pharmacokinetic parameters.
4.  Final result.

``` text
Input Values
    ↓
Intermediate Values
    ↓
Pharmacokinetic Parameters
    ↓
Final Result
```

This improves the understandability and transparency of the calculation
process.

------------------------------------------------------------------------

## 14. Optional Enhancements

The following features are optional and are not required for the core
project:

-   Local calculation history.
-   Camera capture of a fictional laboratory report or medication label.
-   OCR-assisted extraction of a selected value.
-   What-if or scenario simulation.
-   Additional TDM medication module.
-   Simple concentration-time graph.
-   Export or sharing of a calculation summary.


------------------------------------------------------------------------

## 15. Reference Sources

The case study identifies the following starting points for domain
understanding and reference checking:

-   **myTDM Calculator:** https://www.mytdmcalculator.com/
-   **PhIS TDM Calculator Manual:** Malaysian Pharmacy Information
    System documentation covering multiple TDM workflows.
-   **Vancomycin TDM Guidance:** Current authoritative clinical guidance
    should be consulted for any clinical equation or target used in the
    application.

------------------------------------------------------------------------

## 16. Clinical Disclaimer

> **TDM Insight is an academic software prototype.**

The application is intended only for educational and
software-development purposes. It must not be presented as:

-   A clinically validated prescribing system.
-   A diagnostic system.
-   An autonomous treatment-decision system.

All demonstration cases must be fictional, and clinical equations and
reference values must be supported by appropriate authoritative sources.

------------------------------------------------------------------------

## 17. Main Challenge

The main challenge of the project is **not simply building a calculator
screen**. The objective is to turn a non-trivial TDM calculation
workflow into a reliable and understandable mobile application.

### Overall Concept

``` text
Real-World TDM Problem
        ↓
Dynamic Mobile Input
        ↓
Validation
        ↓
Calculation Engine
        ↓
Explainable Results
```

This project therefore combines Android application development, dynamic
form handling, validation, software architecture, pharmacokinetic
calculation logic, and explainable results into one focused mobile
application.
