package com.example.tdminsight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.ln

enum class Method(val title: String, val subtitle: String) { PRE("Pre-dose", "Trough concentration"), POST("Post-dose", "Post-distribution sample"), PRE_POST("Pre + Post", "Two-point concentration") }
enum class Sex(val label: String) { MALE("Male"), FEMALE("Female") }
data class TdmInput(val patient: String, val sex: Sex, val weight: Double, val age: Double, val scr: Double, val dose: Double, val interval: Double, val pre: Double?, val post: Double?, val postTime: Double, val infusion: Double)
data class TdmResult(val method: Method, val ke: Double, val halfLife: Double, val vd: Double, val clearance: Double, val auc24: Double, val estimatedPeak: Double, val explanation: List<String>)

object TdmEngine {

    /*
     * postTime is measured from dose start.
     *
     * Example:
     *   Dose starts at 08:00
     *   Infusion ends at 10:00
     *   Post sample at 12:00
     *
     *   postTime = 4 hours
     *   infusion = 2 hours
     */

    fun validate(i: TdmInput, method: Method): List<String> = buildList {
        if (i.patient.isBlank()) {
            add("Enter a fictional patient/case name.")
        }

        if (i.weight !in 20.0..250.0) {
            add("Weight must be between 20 and 250 kg.")
        }

        if (i.age !in 1.0..120.0) {
            add("Age must be between 1 and 120 years.")
        }

        if (i.scr !in 0.1..15.0) {
            add("Serum creatinine must be between 0.1 and 15 mg/dL.")
        }

        if (i.dose <= 0.0) {
            add("Dose must be greater than zero.")
        }

        if (i.interval !in 4.0..72.0) {
            add("Dosing interval must be 4–72 hours.")
        }

        if (i.infusion !in 0.25..4.0) {
            add("Infusion duration must be 0.25–4 hours.")
        }

        if (i.infusion >= i.interval) {
            add("Infusion duration must be shorter than the dosing interval.")
        }

        if (method != Method.POST && (i.pre == null || i.pre <= 0.0)) {
            add("Enter a valid pre-dose concentration.")
        }

        if (method != Method.PRE && (i.post == null || i.post <= 0.0)) {
            add("Enter a valid post-dose concentration.")
        }

        if (method != Method.PRE) {
            if (i.postTime <= i.infusion) {
                add("Post sample time must be after infusion completion.")
            }

            if (i.postTime >= i.interval) {
                add("Post sample time must be before the next dose.")
            }
        }

        if (method == Method.PRE_POST) {
            val pre = i.pre ?: 0.0
            val post = i.post ?: 0.0

            if (post <= pre) {
                add(
                    "For this model, post-dose concentration must exceed " +
                            "pre-dose concentration."
                )
            }

            val eliminationTime = i.interval - i.postTime

            if (eliminationTime <= 0.0) {
                add("The time between post-dose sampling and the next dose must be positive.")
            }
        }
    }

    fun calculate(i: TdmInput, method: Method): TdmResult {
        val explanation = mutableListOf<String>()

        require(validate(i, method).isEmpty()) {
            validate(i, method).joinToString("; ")
        }

        /*
         * Cockcroft-Gault creatinine clearance.
         *
         * Serum creatinine is entered in mg/dL.
         * CrCl result is mL/min.
         */
        val sexFactor = if (i.sex == Sex.FEMALE) 0.85 else 1.0

        val crCl = (
                (140.0 - i.age) *
                        i.weight *
                        sexFactor
                        / (72.0 * i.scr)
                ).coerceAtLeast(0.0)

        explanation +=
            "Estimated Cockcroft-Gault CrCl = ${fmt(crCl)} mL/min."

        /*
         * Population Ke equation from the worksheet:
         *
         * Ke = 0.0044 + (CrCl × 0.00083)
         *
         * Do not divide this result by 24.
         */
        val estimatedKe = 0.0044 + (crCl * 0.00083)

        require(estimatedKe > 0.0 && estimatedKe.isFinite()) {
            "Unable to calculate a valid elimination rate constant."
        }

        val ke = if (method == Method.PRE_POST) {
            val pre = requireNotNull(i.pre)
            val post = requireNotNull(i.post)

            /*
             * Pre-dose sample is assumed to be immediately before
             * the next dose.
             *
             * postTime is from dose start, so the elimination interval is:
             *
             * interval - postTime
             *
             * Example:
             * interval = 12 h
             * post sample = 4 h after dose start
             * elimination interval = 8 h
             */
            val eliminationTime = i.interval - i.postTime

            require(post > pre) {
                "Post-dose concentration must be greater than pre-dose concentration."
            }

            require(eliminationTime > 0.0) {
                "The elimination interval must be positive."
            }

            val measuredKe = ln(post / pre) / eliminationTime

            require(measuredKe > 0.0 && measuredKe.isFinite()) {
                "Measured concentration data do not produce a valid Ke."
            }

            explanation +=
                "Measured Ke = ln(Cpost / Cpre) ÷ " +
                        "(interval − post sample time)."

            measuredKe
        } else {
            explanation +=
                "Estimated Ke = 0.0044 + (CrCl × 0.00083). " +
                        "The result is not divided by 24."

            estimatedKe
        }

        val halfLife = ln(2.0) / ke

        /*
         * Estimate Vd.
         *
         * PRE:
         * A trough alone cannot identify Vd, so use the explicit
         * population assumption of 0.7 L/kg.
         *
         * POST and PRE_POST:
         * For intermittent IV infusion:
         *
         * Vd =
         * Dose × (1 − exp(−Ke × T))
         * --------------------------------
         * T × Ke × Cmax
         *
         * Cmax is extrapolated to the end of infusion.
         */
        val vd: Double

        if (method == Method.PRE) {
            vd = 0.7 * i.weight

            explanation +=
                "Vd = 0.7 L/kg × body weight because a pre-dose " +
                        "concentration alone cannot identify patient-specific Vd."
        } else {
            val post = requireNotNull(i.post)

            val timeAfterInfusion =
                i.postTime - i.infusion

            require(timeAfterInfusion >= 0.0) {
                "Post-dose sample must occur at or after infusion completion."
            }

            /*
             * Extrapolate the measured post concentration backward
             * to the end of infusion.
             */
            val cMaxEnd =
                post * exp(ke * timeAfterInfusion)

            val infusionFactor =
                1.0 - exp(-ke * i.infusion)

            require(cMaxEnd > 0.0 && cMaxEnd.isFinite()) {
                "Unable to calculate end-of-infusion concentration."
            }

            require(infusionFactor > 0.0 && infusionFactor.isFinite()) {
                "Unable to calculate the infusion correction factor."
            }

            /*
             * Correct intermittent-infusion Vd equation.
             */
            vd =
                (
                        i.dose * infusionFactor
                                / (i.infusion * ke * cMaxEnd)
                        )

            explanation +=
                "Vd = Dose × (1 − exp(−Ke × infusion time)) " +
                        "÷ (infusion time × Ke × Cmax at end of infusion)."
        }

        require(vd > 0.0 && vd.isFinite()) {
            "Calculated volume of distribution is invalid."
        }

        /*
         * Clearance:
         *
         * CL = Ke × Vd
         */
        val clearance = ke * vd

        require(clearance > 0.0 && clearance.isFinite()) {
            "Calculated clearance is invalid."
        }

        /*
         * AUC24:
         *
         * AUC24 =
         * Dose per interval × 24
         * ----------------------
         * Dosing interval × Clearance
         *
         * Equivalent:
         *
         * AUC24 = daily dose / Clearance
         */
        val auc24 =
            (i.dose * 24.0) /
                    (i.interval * clearance)

        require(auc24 > 0.0 && auc24.isFinite()) {
            "Calculated AUC24 is invalid."
        }

        /*
         * Single-dose end-of-infusion peak estimate.
         *
         * This is an educational estimate, not a validated
         * patient-specific dosing recommendation.
         */
        val estimatedPeak =
            (
                    i.dose /
                            (clearance * i.infusion)
                    ) * (1.0 - exp(-ke * i.infusion))

        require(estimatedPeak > 0.0 && estimatedPeak.isFinite()) {
            "Calculated peak concentration is invalid."
        }

        explanation +=
            "Half-life = ln(2) ÷ Ke = ${fmt(halfLife)} h."

        explanation +=
            "Clearance = Ke × Vd = ${fmt(clearance)} L/h."

        explanation +=
            "AUC24 = Dose × 24 ÷ (interval × clearance) " +
                    "= ${fmt(auc24)} mg·h/L."

        explanation +=
            "All outputs are educational one-compartment estimates " +
                    "and must not be used alone for clinical dose decisions."

        /*
         * Safety guard against the original failure mode.
         */
        require(vd >= 10.0) {
            "Calculated Vd is implausibly low. Check dose, concentration, " +
                    "sample timing, and units."
        }

        require(clearance >= 0.1) {
            "Calculated clearance is implausibly low. Check renal inputs, " +
                    "sample timing, and units."
        }

        require(halfLife <= 48.0) {
            "Calculated half-life is implausibly long. Check Ke, " +
                    "sample timing, and units."
        }

        return TdmResult(
            method = method,
            ke = ke,
            halfLife = halfLife,
            vd = vd,
            clearance = clearance,
            auc24 = auc24,
            estimatedPeak = estimatedPeak,
            explanation = explanation
        )
    }
}

fun fmt(value: Double) = "%.2f".format(value)

@Composable fun HomePage(onStart: () -> Unit) {
    Text("WELCOME TO", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    Text("TDM Insight", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Teal), contentAlignment = Alignment.Center) { Icon(Icons.Default.MedicalServices, null, tint = Color.White, modifier = Modifier.size(27.dp)) }
                Column(Modifier.padding(start = 13.dp)) { Text("Vancomycin TDM", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Clinical monitoring workspace", color = Slate, fontSize = 12.sp) }
                Spacer(Modifier.weight(1f)); Icon(Icons.Default.Lock, "Offline and private", tint = Teal, modifier = Modifier.size(19.dp))
            }
            Text("Calculate with confidence.", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Organize patient data, sampling times, and explainable PK estimates in one focused workflow.", color = Slate, fontSize = 14.sp, lineHeight = 20.sp)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Mint).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Timeline, null, tint = Teal, modifier = Modifier.size(23.dp)); Column(Modifier.padding(start = 10.dp)) { Text("Guided in 3 simple steps", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Method  •  Details  •  Results", color = Slate, fontSize = 11.sp) } }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Color.White)) { Text("Start new assessment", fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp)) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { TrustCard("OFFLINE", "Private by design", Icons.Default.Shield, Modifier.weight(1f)); TrustCard("3 STEPS", "Guided workflow", Icons.Default.Timeline, Modifier.weight(1f)) }
    Section("Built for a careful review") {
        FeatureRow(Icons.Default.Person, "Structured patient profile", "Keep key context together")
        FeatureRow(Icons.Default.Science, "Method-aware inputs", "Only see fields you need")
        FeatureRow(Icons.Default.TrendingUp, "Explainable results", "Review each intermediate value")
    }
    InfoBanner("Academic prototype only", "Use fictional cases. Not a prescribing or treatment tool.")
}

@Composable fun TrustCard(label: String, caption: String, icon: ImageVector, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Teal, modifier = Modifier.size(22.dp)); Column(Modifier.padding(start = 10.dp)) { Text(label, color = Teal, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(caption, color = Slate, fontSize = 11.sp) } } } }
@Composable fun FeatureRow(icon: ImageVector, title: String, caption: String) { Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).clip(CircleShape).background(Mint), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal, modifier = Modifier.size(19.dp)) }; Column(Modifier.padding(start = 12.dp)) { Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(caption, color = Slate, fontSize = 12.sp) } } }

@Composable fun MethodPage(method: Method, onMethod: (Method) -> Unit, onBack: () -> Unit, onContinue: () -> Unit) { StepIndicator(1); PageTitle("Choose a sampling method", "Select the workflow that matches your available concentration data."); Section("Sampling workflow") { Method.entries.forEach { m -> MethodChoice(m, method == m) { onMethod(m) } } }; InfoBanner("Tip", "Use Pre + Post when both concentrations are available for a two-point estimate."); ActionRow("Continue", onBack, onContinue); Disclaimer() }

@Composable fun InputPage(method: Method, patient: String, setPatient: (String) -> Unit, sex: Sex, setSex: (Sex) -> Unit, weight: String, setWeight: (String) -> Unit, age: String, setAge: (String) -> Unit, scr: String, setScr: (String) -> Unit, dose: String, setDose: (String) -> Unit, interval: String, setInterval: (String) -> Unit, infusion: String, setInfusion: (String) -> Unit, pre: String, setPre: (String) -> Unit, post: String, setPost: (String) -> Unit, postTime: String, setPostTime: (String) -> Unit, errors: List<String>, onBack: () -> Unit, onCalculate: () -> Unit) {
    StepIndicator(2); PageTitle("Enter assessment data", "Complete the required fields for this fictional case.")
    Section("Patient profile") { Field("Case name", patient, setPatient, KeyboardType.Text, "e.g. Demo patient", Icons.Default.Person); Text("Sex", color = Slate, fontSize = 12.sp); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Sex.entries.forEach { option -> FilterChip(selected = sex == option, onClick = { setSex(option) }, label = { Text(option.label) }, modifier = Modifier.weight(1f), leadingIcon = if (sex == option) ({ Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)) }) else null) } }; Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { Field("Weight", weight, setWeight, KeyboardType.Decimal, "kg") }; Box(Modifier.weight(1f)) { Field("Age", age, setAge, KeyboardType.Decimal, "years") } }; Field("Serum creatinine", scr, setScr, KeyboardType.Decimal, "mg/dL") }
    Section("Medication regimen") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { Field("Dose", dose, setDose, KeyboardType.Decimal, "mg") }; Box(Modifier.weight(1f)) { Field("Interval", interval, setInterval, KeyboardType.Decimal, "hours") } }; Field("Infusion duration", infusion, setInfusion, KeyboardType.Decimal, "hours", Icons.Default.AccessTime) }
    Section("Sampling data • ${method.title}") { if (method != Method.POST) Field("Pre-dose concentration", pre, setPre, KeyboardType.Decimal, "mg/L", Icons.Default.Science); if (method != Method.PRE) { Field("Post-dose concentration", post, setPost, KeyboardType.Decimal, "mg/L", Icons.Default.Science); Field("Post sample time from dose start", postTime, setPostTime, KeyboardType.Decimal, "hours", Icons.Default.AccessTime) } }
    if (errors.isNotEmpty()) ErrorBox(errors)
    ActionRow("Calculate results", onBack, onCalculate); Disclaimer()
}

@Composable fun PageTitle(title: String, subtitle: String) { Text(title, color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Slate, fontSize = 14.sp, lineHeight = 20.sp) }
@Composable fun StepIndicator(step: Int) { Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { listOf("Method", "Details", "Results").forEachIndexed { index, label -> val active = index <= step; Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) { Box(Modifier.size(28.dp).clip(CircleShape).background(if (active) Teal else Mist), contentAlignment = Alignment.Center) { if (index < step) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp)) else Text("${index + 1}", color = if (active) Color.White else Slate, fontSize = 12.sp, fontWeight = FontWeight.Bold) }; Text(label, color = if (active) Teal else Slate, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) }; if (index < 2) HorizontalDivider(Modifier.weight(.45f).padding(bottom = 18.dp), color = if (index < step) Teal else Line) } } }
@Composable fun MethodChoice(method: Method, selected: Boolean, onClick: () -> Unit) { val icon = when (method) { Method.PRE -> Icons.Default.Science; Method.POST -> Icons.Default.AccessTime; Method.PRE_POST -> Icons.Default.Timeline }; Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(if (selected) Mint else Color.White), border = BorderStroke(1.dp, if (selected) Teal else Line), elevation = CardDefaults.cardElevation(0.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (selected) Teal else Mist), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (selected) Color.White else Teal) }; Column(Modifier.padding(start = 13.dp).weight(1f)) { Text(method.title, fontWeight = FontWeight.Bold, color = Ink); Text(method.subtitle, color = Slate, fontSize = 12.sp) }; if (selected) Icon(Icons.Default.CheckCircle, "Selected", tint = Teal) } } }
@Composable fun Section(title: String, content: @Composable ColumnScope.() -> Unit) { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line), elevation = CardDefaults.cardElevation(0.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) { Text(title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp); content() } } }
@Composable fun Field(label: String, value: String, onChange: (String) -> Unit, type: KeyboardType, placeholder: String, icon: ImageVector? = null) { OutlinedTextField(value, onChange, label = { Text(label) }, placeholder = { Text(placeholder) }, leadingIcon = icon?.let { { Icon(it, null, tint = Teal) } }, keyboardOptions = KeyboardOptions(keyboardType = type), singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal, focusedLabelColor = Teal, cursorColor = Teal)) }
@Composable fun ActionRow(primary: String, onBack: () -> Unit, onPrimary: () -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onBack, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Back") }; Button(onClick = onPrimary, Modifier.weight(1.45f).height(50.dp), shape = RoundedCornerShape(14.dp)) { Text(primary, fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp)); Icon(if (primary.startsWith("Calculate")) Icons.Default.Calculate else Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp)) } } }
@Composable fun ErrorBox(errors: List<String>) { Card(colors = CardDefaults.cardColors(Color(0xFFFFF1F0)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFF4C7C3))) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("Check the highlighted information", fontWeight = FontWeight.Bold, color = Color(0xFF9B2C2C)); errors.forEach { Text("• $it", color = Color(0xFF9B2C2C), fontSize = 13.sp) } } } }

@Composable fun ResultScreen(r: TdmResult, onReset: () -> Unit) { StepIndicator(3); PageTitle("Assessment results", "${r.method.title} workflow • educational estimate"); Card(colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.TrendingUp, null, tint = Aqua); Text("PRIMARY RESULT", color = Aqua, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) }; Text("Estimated AUC24", color = Color.White.copy(.75f), fontSize = 14.sp); Text("${fmt(r.auc24)}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("mg·h/L", color = Aqua, fontSize = 14.sp); Text("For review only — not a dosing recommendation", color = Color.White.copy(.65f), fontSize = 12.sp) } }
    Section("Pharmacokinetic parameters") { ResultRow("Elimination rate constant (Ke)", "${fmt(r.ke)} /h"); ResultRow("Elimination half-life", "${fmt(r.halfLife)} h"); ResultRow("Volume of distribution", "${fmt(r.vd)} L"); ResultRow("Clearance", "${fmt(r.clearance)} L/h"); ResultRow("Estimated peak", "${fmt(r.estimatedPeak)} mg/L") }
    var expanded by remember { mutableStateOf(false) }; Section("Calculation explanation") { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(19.dp)); Text("Transparent intermediate values", color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp)) }; TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide calculation steps" else "Show calculation steps"); Icon(Icons.Default.ExpandMore, null) }; if (expanded) r.explanation.forEachIndexed { n, s -> Text("${n + 1}. $s", fontSize = 13.sp, color = Slate, lineHeight = 19.sp) } }
    Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Start new assessment", fontWeight = FontWeight.Bold) }; Disclaimer()
}
@Composable fun ResultRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Slate, fontSize = 13.sp); Text(value, fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp) } }
@Composable fun InfoBanner(title: String, message: String) { Card(colors = CardDefaults.cardColors(Mint), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(19.dp)); Column(Modifier.padding(start = 9.dp)) { Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(message, color = Slate, fontSize = 12.sp, lineHeight = 17.sp) } } } }
@Composable fun Disclaimer() { Text("Academic prototype only. Not clinically validated and must not be used for prescribing, diagnosis, or treatment decisions.", fontSize = 11.sp, color = Slate, lineHeight = 15.sp) }
